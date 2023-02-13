# https://hub.docker.com/r/migelalfa/ansible/tags 

/home/migel/devops/mikrotik: <- что монтируем (путь) you must specify the directory where it is located ansible-playbook

/tmp/micro:rw <- куда монтируем where inside the container we will mount

1) copy dockerfile with ansible
2) run docker with you mout path where is ansible-playbook
3) chande dir in docker with ansible
4) run ansible-playbook
5) copy passwords in outside dir what whas mount.


```

1) docker pull migelalfa/ansible:latest

2) docker run -v /home/migel/devops:/tmp/micro:rw -it migelalfa/ansible:latest

3) cd /tmp/micro

4) ansible-playbook -i inv.yaml routeros.yaml --ask-vault-pass

5) cp -r /tmp/passwords/ /tmp/micro/

```

