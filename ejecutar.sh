#!/bin/sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR_FILE="$ROOT_DIR/target/gestor-contrasenas-1.0.0.jar"

if [ ! -f "$JAR_FILE" ]; then
    (cd "$ROOT_DIR" && mvn -q -DskipTests package)
fi

cd "$ROOT_DIR"
exec java -jar "$JAR_FILE" "$@"