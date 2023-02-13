FROM centos:7

ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8
ENV PIP_ROOT_USER_ACTION=ignore

RUN yum check-update; \
    yum install -y gcc libffi-devel python3 epel-release; \
    yum install -y python3-pip; \
    yum install -y wget; \
    yum install -y openssh-server openssh-clients; \
    yum clean all
    

RUN pip3 install --upgrade pip; \
    pip3 install --upgrade virtualenv; \
    pip3 install pywinrm[kerberos]; \
    pip3 install pywinrm; \
    pip3 install jmspath; \
    pip3 install requests; \
    pip3 install paramiko; \
    python3 -m pip install ansible; \