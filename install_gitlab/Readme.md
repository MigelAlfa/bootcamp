# ansible version
```
ansible [core 2.12.5]
  config file = /etc/ansible/ansible.cfg
  python version = 3.8.10 (default, Jun 22 2022, 20:18:18) [GCC 9.4.0]
  jinja version = 2.11.3
  libyaml = True

```
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