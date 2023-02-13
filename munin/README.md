# 1) First using 
Two roles in munin.yaml (use apache and munin ) 
```
Example

---
- hosts: boot
  roles:
    - apache
    - munin
    #- munin_node
```

This Roles must be deployed on master-node (one of the ip addresses in inventoty)

```
Example

boot:
  vars:
    ansible_user: user
  children:
    it_boot:
      hosts:
        node1:
          ansible_host: 192.168.100.100
          ansible_node_name: node1
        #node2:
        #  ansible_host: 192.168.100.101
        #  ansible_node_name: node2
        #node3:
        #  ansible_host: 192.168.100.102
        #  ansible_node_name: node3
```

In the file /munin/roles/munin/defauls/main.yaml you must make changes as below
where:
 - name: name of node  (then use this name in inventary)
 - adress: ip adress node

```
munin_hosts:
  - name: "node1"
    address: "192.168.100.101"

  - name: "node2"
    address: "192.168.100.102"

  - name: "node3"
    address: "192.168.100.103"

```
# 2) Second
Next one role in munin.yaml (munin node)

This Role must be deployed on master-node and other nodes, what was you want to monitoring

In the file /munin/roles/munin-node/defauls/main.yaml

you must make changes as below, where ip adress is adress master-node

```
munin_node_allowed_ips:
  - '^192\.168\.100\.108$' <--- The address of the master node should be specified here!
  - '^::1$'
```

# 3) Verify ufw or other firewall and allow port 4949

# 4) User http//master_node_ip/munin/ to get access to munin

# 5) Reboot munin or whait 5 min. 
You can see something similar as a Screen

PS. Not working? You can all time up you soft skill and find all answers in google.