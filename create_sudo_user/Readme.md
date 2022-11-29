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