#!/bin/bash

# This script is used to publish release from the private to the public repository master (usually
# by travis).

# Exit on any error
set -e
# Print out commands
set -o xtrace

TARGET_REPO_SLUG=workaroundgmbh/proglove_connect_samples

# Cleaning current branch in case files were changed which could cause merge conflicts
git reset HEAD --hard
# Add target (public) repo to remotes
git remote add target https://${TARGET_REPO_SLUG%/*}:${GITHUB_API_KEY}@github.com/${TARGET_REPO_SLUG}.git
git fetch --all
# Checkout target master branch
git checkout target/master
git branch -f master
git checkout master
# Collect all unpublished changes from private master
git diff master origin/master --binary > patch.diff
# Apply the changes
git apply patch.diff
# Remove the patch file
rm patch.diff
# Commit collected changes using the metadata of the private master's last commit
git add -A
git commit -C origin/master
# Push new code to target repo
git push target master

