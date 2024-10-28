#!/bin/bash

set -e

DOCKER_CMD="docker"
DOCKER_URL="https://get.docker.com"
DOCKER_DIR="$(dirname "$0")"

if ! command -v "$DOCKER_CMD" &> /dev/null; then
    echo "Docker is not installed. Installing Docker..."

    curl -fsSL "$DOCKER_URL" -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh

    echo "Docker installed successfully."
else
    echo "Docker is already installed."
fi

if ! systemctl is-active --quiet docker; then
    echo "Starting Docker service..."
    sudo systemctl start docker
fi

if ! groups $(whoami) | grep -q "\bdocker\b"; then
    echo "Adding current user to the 'docker' group..."
    sudo usermod -aG docker $(whoami)
    echo "You may need to log out and log back in for group changes to take effect."
fi

chmod +x ./mvnw

echo "Building and running the Spring Boot application..."
./mvnw clean package spring-boot:run
