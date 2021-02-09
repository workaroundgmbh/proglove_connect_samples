#!/bin/bash

# This script is used to publish release from the private to the public repository master (usually
# by travis).

# Exit on any error
set -e
# Print out commands
set -o xtrace

TARGET=git@github.com:workaroundgmbh/proglove_connect_samples.git

# Cleaning current branch in case files were changed which could cause merge conflicts
git reset HEAD --hard
# Add target (public) repo to remotes
git remote add target $TARGET
git fetch --all
# Checkout target master branch
git checkout target/master
git branch -f master
git checkout master
# Squash all unpublished changes from private master
git merge --squash origin/master
# Commit squashed changes using the metadata of the private master's last commit
git commit -C origin/master
# Push new code to target repo
git push target master

