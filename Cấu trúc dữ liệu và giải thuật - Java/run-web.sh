#!/bin/bash
# Biên dịch và chạy Web UI (backend Java + frontend) tại http://localhost:8080
set -e
cd "$(dirname "${BASH_SOURCE[0]}")"
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src -name '*.java')
echo "Mở trình duyệt tại: http://localhost:${1:-8080}"
java -Dfile.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.web.ApiServer "${1:-8080}" web
