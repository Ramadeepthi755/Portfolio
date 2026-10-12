#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
ENV_FILE="$ROOT/backend/.env"
if [[ ! -f "$ENV_FILE" ]]; then
  cp "$ROOT/backend/.env.example" "$ENV_FILE"
  echo "Created backend/.env with local defaults. Edit it if your MySQL root account uses a password."
fi
set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a
: "${ADMIN_EMAIL:?Set ADMIN_EMAIL in backend/.env}"
: "${ADMIN_PASSWORD:?Set ADMIN_PASSWORD in backend/.env}"
if [[ ${#ADMIN_PASSWORD} -lt 12 ]]; then echo "ADMIN_PASSWORD must be at least 12 characters."; exit 1; fi
: "${DATABASE_USERNAME:=root}"
: "${DATABASE_URL:=jdbc:mysql://localhost:3306/portfolio?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}"
: "${PORT:=8080}"
if ! command -v mvn >/dev/null 2>&1; then echo "Maven is required. Install Maven and retry."; exit 1; fi
if ! command -v npm >/dev/null 2>&1; then echo "Node.js/npm is required. Install Node.js and retry."; exit 1; fi
if ! command -v mysql >/dev/null 2>&1; then echo "MySQL client not found. Install MySQL and ensure the mysql command is available."; exit 1; fi
export MYSQL_PWD="${DATABASE_PASSWORD:-}"
if ! mysql -h localhost -u "$DATABASE_USERNAME" -e 'SELECT 1' >/dev/null 2>&1; then
  unset MYSQL_PWD
  echo "Cannot connect to MySQL. Check DATABASE_USERNAME and DATABASE_PASSWORD in backend/.env, and make sure MySQL is running."
  exit 1
fi
if ! mysql -h localhost -u "$DATABASE_USERNAME" -e 'CREATE DATABASE IF NOT EXISTS portfolio CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;'; then
  unset MYSQL_PWD
  echo "Could not create the portfolio database. Check MySQL privileges."
  exit 1
fi
unset MYSQL_PWD
cleanup(){ [[ -n "${API_PID:-}" ]] && kill "$API_PID" 2>/dev/null || true; }
trap cleanup EXIT INT TERM
cd "$ROOT/backend"
echo "Starting Spring Boot API on port $PORT ..."
mvn spring-boot:run & API_PID=$!
for i in $(seq 1 60); do
  if curl -fsS "http://localhost:$PORT/api/health" >/dev/null 2>&1; then break; fi
  if ! kill -0 "$API_PID" 2>/dev/null; then echo "Backend exited before becoming ready. Check Maven output above."; exit 1; fi
  sleep 2
done
if ! curl -fsS "http://localhost:$PORT/api/health" >/dev/null 2>&1; then echo "Backend did not become ready within 120 seconds."; exit 1; fi
cd "$ROOT"
if [[ ! -d node_modules ]]; then npm install; fi
echo "Open the exact Local URL printed by Vite below (it may use 5174 if 5173 is busy)."
echo "Admin dashboard: append /admin to that Vite Local URL."
echo "Local admin email: $ADMIN_EMAIL"
echo "Local admin password: configured in backend/.env (change before deployment)"
npm run dev
