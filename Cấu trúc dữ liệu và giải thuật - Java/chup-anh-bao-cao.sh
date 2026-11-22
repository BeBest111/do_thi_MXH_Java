#!/bin/bash
# Chụp lại toàn bộ ảnh giao diện cho báo cáo (độ phân giải gấp đôi, nền sáng).
# Yêu cầu: server đang chạy ở http://localhost:8080 (run-web.bat) và có Microsoft Edge.
cd "$(dirname "$0")"
EDGE="/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"
PROF="$(mktemp -d)"
mkdir -p docs/screenshots
shot(){ H=${3:-880}; "$EDGE" --headless=new --disable-gpu --hide-scrollbars --user-data-dir="$PROF" \
  --window-size=1280,$H --force-device-scale-factor=2.25 --virtual-time-budget=10000 \
  --screenshot="$(cygpath -w "$PWD/docs/screenshots/$1")" "http://localhost:8080/$2" 2>/dev/null; echo "Da chup $1"; }
shot 01_main.png ""
shot 02_mutual.png "?demo=mutual"
shot 03_recommend_bfs.png "?demo=bfs"
shot 04_recommend_dfs.png "?demo=dfs"
shot 05_path.png "?demo=path"
shot 06_manage.png "?demo=manage" 1060
rm -rf "$PROF"
