#!/bin/bash

set -e

JAVA_VERSION=21
JAVA_URL=https://download.oracle.com/java/21/latest/jdk-21_linux-x64_bin.tar.gz
JAVA_DIR="$(dirname "$0")/java"

if ! command -v curl &> /dev/null; then
    echo "curl is not installed. Please install curl and try again."
    exit 1
fi

mkdir -p "$JAVA_DIR"

if [ ! -d "$JAVA_DIR/jdk-$JAVA_VERSION" ]; then
    curl -L -o "$JAVA_DIR/jdk.tar.gz" "$JAVA_URL"
    tar -xzf "$JAVA_DIR/jdk.tar.gz" -C "$JAVA_DIR"

    JDK_DIR=$(find "$JAVA_DIR" -maxdepth 1 -type d -name "jdk-*")

    if [ -z "$JDK_DIR" ]; then
        echo "Failed to extract JDK. Check if the download was successful."
        exit 1
    fi

    mv "$JDK_DIR" "$JAVA_DIR/jdk-$JAVA_VERSION"
    rm "$JAVA_DIR/jdk.tar.gz"
fi

export JAVA_HOME="$JAVA_DIR/jdk-$JAVA_VERSION"
export PATH="$JAVA_HOME/bin:$PATH"

chmod +x ./mvnw

java -version

./mvnw clean package spring-boot:run

