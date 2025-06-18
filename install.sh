#!/bin/bash

# Проверяем, запущен ли скрипт с правами root
if [ "$EUID" -ne 0 ]; then
    echo "Please run as root (use sudo)"
    exit 1
fi

# Обновляем систему
echo "Updating system packages..."
apt-get update
apt-get upgrade -y

# Устанавливаем Java 17
echo "Installing Java 17..."
apt-get install -y software-properties-common
add-apt-repository -y ppa:linuxuprising/java
apt-get update
apt-get install -y openjdk-17-jdk

# Проверяем установку Java
java -version

# Устанавливаем Maven
echo "Installing Maven..."
apt-get install -y maven

# Проверяем установку Maven
mvn -version

# Устанавливаем Docker
echo "Installing Docker..."
apt-get install -y apt-transport-https ca-certificates curl software-properties-common
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io

# Проверяем установку Docker
docker --version

# Устанавливаем Docker Compose
echo "Installing Docker Compose..."
curl -L "https://github.com/docker/compose/releases/download/v2.24.5/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Проверяем установку Docker Compose
docker-compose --version

# Добавляем текущего пользователя в группу docker
usermod -aG docker $SUDO_USER

# Устанавливаем git
apt-get install -y git

echo "Installation completed!"
echo "Please log out and log back in for the docker group changes to take effect."