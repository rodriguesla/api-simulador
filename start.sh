#!/bin/bash
set -e

# Build the project
mvn clean package

# Start Docker Compose
docker-compose up --build