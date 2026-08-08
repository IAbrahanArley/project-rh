#!/bin/sh
set -eu

: "${FRONTEND_API_BASE_URL:=http://localhost:8080}"

envsubst '${FRONTEND_API_BASE_URL}' \
  < /etc/nginx/templates/runtime-config.template.js \
  > /usr/share/nginx/html/assets/runtime-config.js

exec nginx -g "daemon off;"
