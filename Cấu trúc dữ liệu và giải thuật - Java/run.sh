#!/bin/bash
# Script chạy chương trình Phân tích mạng xã hội bằng đồ thị
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
cd "$DIR"

mkdir -p bin
echo "=== ĐANG BIÊN DỊCH MÃ NGUỒN JAVA ==="
javac -encoding UTF-8 -d bin src/ptit/dsa/socialnetwork/model/*.java \
      src/ptit/dsa/socialnetwork/graph/*.java \
      src/ptit/dsa/socialnetwork/analysis/*.java \
      src/ptit/dsa/socialnetwork/app/*.java \
      src/ptit/dsa/socialnetwork/*.java \
      src/ptit/dsa/socialnetwork/test/*.java

echo "Biên dịch thành công!"
echo "1. Chạy chương trình ứng dụng Menu tương tác (Main)"
echo "2. Chạy bộ kiểm thử tự động toàn diện (SocialNetworkTest)"
read -p "Lựa chọn của bạn (1/2): " mode

if [ "$mode" = "2" ]; then
    java -cp bin ptit.dsa.socialnetwork.test.SocialNetworkTest
else
    java -cp bin ptit.dsa.socialnetwork.Main
fi
