#!/bin/bush

git add .
echo "Enter commit to github"
read commit
git commit -m "$commit"
git push
