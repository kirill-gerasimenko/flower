# 0.3.6 - 2018-01-16

- Fixed issue with existing tags deletion in TFS tracker task upserting.

# 0.3.5 - 2018-01-15

- Fixed issue with TFS tracker task upserting.

# 0.3.3 - 2018-01-12

- Added ability to get pull request commits.
- Added ability to get pull request files.

# 0.3.1 - 2017-12-13

- Added ability to create new tasks in all task trackers.
- Added support for description field in tasks.

# 0.3.0 - 2017-12-11

- Added ability to fill in repository and tracker types manually for shorthand notation.
- Decomposed project into smaller components.
- Added Leiningen template to create new applications instantly.

# 0.2.1 - 2017-12-06

- Added ability to use GitHub user repositories along with organization ones.
- Changed project namespace to "flower" on Clojars.

# 0.2.0 - 2017-12-05

- Added shorthand notations to create single records for trackers, repositories and messaging.
- Added macro to supply default credentials while using shorthand notations.

# 0.1.6 - 2017-11-30

- Added ability to change state and attributes for tracker tasks.
- Fixed tracker protocols to avoid collisions.

# 0.1.5 - 2017-11-23

- Updated dependencies to the most recent versions.

# 0.1.4 - 2017-11-09

- Fixed issue with impure functions.
- Fixed issue with tags for GitHub issues.

# 0.1.3 - 2017-10-20

- Added ability to merge pull requests for GitLab and GitHub.

# 0.1.0 - 2017-10-09

- Added initial tracker support for Jira, GitLab, GitHub and TFS.
- Added initial repository support for GitLab and GitHub.
- Added initial messaging support for Exchange.
