(ns flower.tracker.core
  (:require [clojure.string :as string]
            [com.stuartsierra.component :as component]
            [cemerick.url :as url]
            [flower.common :as common]
            [flower.tracker.proto :as proto]
            [flower.tracker.github.tracker :as github.tracker]
            [flower.tracker.github.task :as github.task]
            [flower.tracker.gitlab.tracker :as gitlab.tracker]
            [flower.tracker.gitlab.task :as gitlab.task]
            [flower.tracker.jira.tracker :as jira.tracker]
            [flower.tracker.jira.task :as jira.task]
            [flower.tracker.tfs.tracker :as tfs.tracker]
            [flower.tracker.tfs.task :as tfs.task]))


;;
;; Public definitions
;;

(defrecord TrackerComponent [auth context]
  component/Lifecycle
  (start [component] (into component {:auth auth
                                      :context context}))
  (stop [component] (into component {:auth {}
                                     :context {}})))


(defn trackers [tracker-component trackers]
  (into {}
        (map (fn [[tracker-name {tracker-type :tracker-type
                                 tracker-url :tracker-url
                                 tracker-projects :tracker-projects}]]
               (let [tracker-projects-list (or tracker-projects (list nil))]
                 [tracker-name (map #((case tracker-type
                                        :tfs tfs.tracker/map->TFSTracker
                                        :gitlab gitlab.tracker/map->GitlabTracker
                                        :jira jira.tracker/map->JiraTracker
                                        :github github.tracker/map->GithubTracker)
                                      {:tracker-component tracker-component
                                       :tracker-name tracker-name
                                       :tracker-url tracker-url
                                       :tracker-project %})
                                    tracker-projects-list)]))
             trackers)))


(defn start-component [args]
  (map->TrackerComponent args))


(def ^:dynamic *tracker-type* nil)


(defmacro with-tracker-type [tracker-type & body]
  `(binding [flower.tracker.core/*tracker-type* ~tracker-type]
     ~@body))


(defn get-tracker-info [tracker-full-url]
  (let [tracker-url (url/url tracker-full-url)
        tracker-domain (get tracker-url :host)
        tracker-path-components (string/split (string/replace (get tracker-url :path "/")
                                                              #"(\.git)?/?$"
                                                              "")
                                              #"/")
        tracker-path-first (string/join "/" (take 2 tracker-path-components))
        tracker-path-first-and-second (string/join "/" (take 3 tracker-path-components))
        tracker-path-second (first (drop 2 tracker-path-components))
        tracker-path-third (first (drop 3 tracker-path-components))
        tracker-path-last (last tracker-path-components)]
    (cond (or (= tracker-domain "github.com")
              (= *tracker-type* :github)) {:tracker-type :github
                                           :tracker-url (str (assoc tracker-url
                                                                    :path tracker-path-first))
                                           :tracker-projects [tracker-path-second]
                                           :tracker-name (keyword (str "github-" tracker-domain "-" tracker-path-second))}
          (or (.contains tracker-domain "gitlab")
              (= *tracker-type* :gitlab)) {:tracker-type :gitlab
                                           :tracker-url (str (assoc tracker-url
                                                                    :path ""))
                                           :tracker-projects [tracker-path-second]
                                           :tracker-name (keyword (str "gitlab-" tracker-domain "-" tracker-path-second))}
          (or (.contains tracker-domain "tfs")
              (= *tracker-type* :tfs)) {:tracker-type :tfs
                                        :tracker-url (str (assoc tracker-url
                                                                 :path tracker-path-first-and-second))
                                        :tracker-projects [tracker-path-third]
                                        :tracker-name (keyword (str "tfs-" tracker-domain "-" tracker-path-third))}
          (or (.contains tracker-domain "jira")
              (= *tracker-type* :jira)) {:tracker-type :jira
                                         :tracker-url (str (assoc tracker-url
                                                                  :path ""))
                                         :tracker-projects [tracker-path-second]
                                         :tracker-name (keyword (str "jira-" tracker-domain "-" tracker-path-second))})))


(defn get-tracker [tracker-full-url]
  (let [tracker-info (get-tracker-info tracker-full-url)
        tracker-name (get tracker-info :tracker-name :tracker)]
    (first (get (trackers (start-component {:auth common/*component-auth*
                                            :context common/*component-context*})
                          {tracker-name tracker-info})
                tracker-name))))


(defn task [task-data]
  (let [tracker (get task-data :tracker)
        tracker-type (proto/get-tracker-type tracker)
        task-function (case tracker-type
                        :tfs tfs.task/map->TFSTrackerTask
                        :gitlab gitlab.task/map->GitlabTrackerTask
                        :jira jira.task/map->JiraTrackerTask
                        :github github.task/map->GithubTrackerTask)]
    (task-function task-data)))
