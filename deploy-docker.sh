#!/usr/bin/env bash

./gradlew jar
docker build --pull -t rundeckpro/ansible-plugin:latest -t rundeckpro/ansible-plugin:${TRAVIS_TAG} .
docker login -u="${DOCKER_USERNAME}" -p="${DOCKER_PASSWORD}"
docker push rundeckpro/ansible-plugin
