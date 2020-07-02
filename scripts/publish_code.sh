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
# Checkout master in the current branch (private one).
git checkout master
# Add a tag for the release
git tag build-$TRAVIS_BUILD_NUMBER
# Push tags to the private repo
git push --follow-tags
# Add target (public) repo to remotes
git remote add target $TARGET
git fetch --all
# Checkout target master branch
git checkout target/master
git checkout -b target_master
# Rebase on the private master
git rebase master
# Push new code to target with tag
git push target master --follow-tags

