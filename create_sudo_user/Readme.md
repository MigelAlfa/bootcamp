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
Too create password
sudo apt install mkpasswd
mkpasswd --method=sha-512

Change ip add in inventory

You must change dir where will your id_rsa.pub come from 
```
# How to play

```
ansible-playbook -i inventory.yaml sudo_u --ask-pass (need root passwd)
```