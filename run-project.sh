#!/usr/bin/env bash

# Run Code Arena locally WITHOUT Docker.
# Requires: JDK 17+, Maven, Node 18+, npm.
#
# Backend  -> http://localhost:${BACKEND_PORT:-8080}  (in-memory H2 DB)
# Frontend -> http://localhost:${FRONTEND_PORT:-3000}

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$ROOT_DIR/backend"
FRONTEND_DIR="$ROOT_DIR/frontend"

BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONTEND_PORT="${FRONTEND_PORT:-3000}"

BACKEND_PID=""
FRONTEND_PID=""

require_command() {
  local cmd="$1"

  if ! command -v "$cmd" >/dev/null 2>&1; then
    echo "Error: '$cmd' is required but not installed." >&2
    exit 1
  fi
}

cleanup() {
  local exit_code=$?

  if [[ -n "$BACKEND_PID" ]] && kill -0 "$BACKEND_PID" >/dev/null 2>&1; then
    kill "$BACKEND_PID" >/dev/null 2>&1 || true
  fi

  if [[ -n "$FRONTEND_PID" ]] && kill -0 "$FRONTEND_PID" >/dev/null 2>&1; then
    kill "$FRONTEND_PID" >/dev/null 2>&1 || true
  fi

  wait >/dev/null 2>&1 || true
  exit "$exit_code"
}

trap cleanup EXIT INT TERM

require_command java
require_command mvn
require_command node
require_command npm

if [[ ! -d "$BACKEND_DIR" || ! -d "$FRONTEND_DIR" ]]; then
  echo "Error: expected 'backend' and 'frontend' directories under $ROOT_DIR." >&2
  exit 1
fi

if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
  echo "Installing frontend dependencies..."
  (cd "$FRONTEND_DIR" && npm install)
fi

echo "Starting backend on http://localhost:${BACKEND_PORT} ..."
(
  cd "$BACKEND_DIR"
  SERVER_PORT="$BACKEND_PORT" mvn -q spring-boot:run \
    -Dspring-boot.run.arguments="--server.port=${BACKEND_PORT}"
) &
BACKEND_PID=$!

echo "Starting frontend on http://localhost:${FRONTEND_PORT} ..."
(
  cd "$FRONTEND_DIR"
  PORT="$FRONTEND_PORT" \
  REACT_APP_API_BASE_URL="http://localhost:${BACKEND_PORT}/api" \
  BROWSER=none \
  npm start
) &
FRONTEND_PID=$!

echo
echo "Code Arena is starting up (no Docker)."
echo "Frontend: http://localhost:${FRONTEND_PORT}"
echo "Backend:  http://localhost:${BACKEND_PORT}"
echo "Press Ctrl+C to stop both services."
echo

wait -n "$BACKEND_PID" "$FRONTEND_PID"
