#!/bin/bash

if ! command -v docker &> /dev/null
then
    for pkg in docker.io docker-doc docker-compose podman-docker containerd runc; do sudo apt-get remove -y $pkg; done
    sudo apt update -y
    sudo apt install -y apt-transport-https ca-certificates curl software-properties-common ufw
    sudo install -m 0755 -d /etc/apt/keyrings
    curl -fsSL https://download.docker.com/linux/debian/gpg | sudo tee /etc/apt/keyrings/docker.asc
    sudo chmod a+r /etc/apt/keyrings/docker.asc
    echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
    sudo apt update -y
    sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
	
    if command -v docker &> /dev/null
    then
        sudo usermod -aG docker $USER
        docker --version
    else
        exit 1
    fi
else
    docker --version
fi

if [ -f docker-compose.yml ]; then
    sudo docker compose up -d --build
else
    exit 1
fi

