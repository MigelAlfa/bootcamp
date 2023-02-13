## _Description_

This playbook adds a selfsigned ssl certificate to an existing Teamcity server.

Tested with Ansible 2.12.5

## _How to use_


- Put variables in the https_teamcity/vars/main.yaml 
- Put username and server ip in inv.yaml
- Run: 
```sh
ansible-playbook -i inv.yaml start_role.yml --become --ask-become-pass

```
- Enter the ssh user password
- Enter the password for keystore ( at least 8 symbols)

