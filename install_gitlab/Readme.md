# How to use
```
Change ip add in inventory and user with sudo or root

Change url in dir /roles/gitlab_ce/defaults if need
gitlab_external_url: "https://git.it-academy.by"
```
# How to play
```
ansible-playbook -i inventory.yaml gitlab.yaml --ask-become-pass --become
```