# How to use


need change in
`ansible.cfg`

```

host_key_checking = False

```

To create password

```
sudo apt install mkpasswd
mkpasswd --method=sha-512

```

Change ip/hostname in inventory

# How to play

```
ansible-playbook -i inventory.yaml sudo_user --ask-pass (need root passwd)
```
