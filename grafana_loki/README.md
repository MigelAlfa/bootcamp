ansible version [core 2.14.1]

# Grafana+loki using to take logs from docker conteiners

# First time install docker grafana/loki-docker-driver

```
ansible-playbook -i inv.yaml grafana_plug.yaml -k
```

# Then geploy grafana and loki in docker conteiners

Grafana will be available http://host_adress:3000

```
ansible-playbook -i inv.yaml grafana.yaml -k
```


# Using this parametr when docker container is deployed 
In conteinet whete you'r going too take logs (backend, front, sql)

```
--log-driver=loki --log-opt loki-url="http://192.168.*.*:3100/loki/api/v1/push"
```

