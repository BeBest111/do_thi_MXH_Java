@echo off
chcp 65001 > nul
cd /d "%~dp0"

echo ===============================================================================
echo   HOC VIEN CONG NGHE BUU CHINH VIEN THONG - KHOA CONG NGHE THONG TIN
echo   BAI TAP LON: PHAN TICH MANG XA HOI BANG DO THI (JAVA)
echo ===============================================================================
echo [1/3] Dang chuan bi danh sach tep tin Java...

if not exist bin mkdir bin

(
    echo src/ptit/dsa/socialnetwork/model/User.java
    echo src/ptit/dsa/socialnetwork/graph/SocialGraph.java
    echo src/ptit/dsa/socialnetwork/analysis/FriendRecommendation.java
    echo src/ptit/dsa/socialnetwork/analysis/ConnectionPath.java
    echo src/ptit/dsa/socialnetwork/analysis/SocialAnalyzer.java
    echo src/ptit/dsa/socialnetwork/app/SocialNetworkApp.java
    echo src/ptit/dsa/socialnetwork/Main.java
    echo src/ptit/dsa/socialnetwork/test/SocialNetworkTest.java
) > sources.txt

echo [2/3] Dang bien dich ma nguon Java bang javac...
javac -encoding UTF-8 -d bin @sources.txt

if %errorlevel% neq 0 (
    echo.
    echo [LOI] Bien dich that bai! Vui long kiem tra lai JDK Java tren may.
    pause
    exit /b %errorlevel%
)

if exist sources.txt del sources.txt
echo [3/3] Bien dich thanh cong!
echo.
echo =============================== CHON CHE DO ===============================
echo 1. Chay ung dung Menu tuong tac (Main - SocialNetworkApp)
echo 2. Chay bo kiem thu tu dong toan dien (SocialNetworkTest)
echo ===============================================================================
set /p mode="Nhap lua chon cua ban (1 hoac 2, mac dinh 1): "

if "%mode%"=="2" (
    java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.test.SocialNetworkTest
) else (
    java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.Main
)

echo.
pause
