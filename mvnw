#!/bin/sh
# Maven Wrapper Script
MAVEN_PROJECTBASEDIR=$(dirname "$0")
exec "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/mvnw" "$@" 2>/dev/null || \
  mvn "$@" 2>/dev/null || \
  echo "Instala Java 17+ y recarga VS Code. Luego usa Run > Run Java en BrumaApplication.java"
