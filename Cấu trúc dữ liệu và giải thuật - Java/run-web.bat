@echo off
chcp 65001 > nul
cd /d "%~dp0"
if not exist bin mkdir bin
echo [1/2] Dang bien dich ma nguon Java...
dir /s /b src\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
if %errorlevel% neq 0 (
    echo [LOI] Bien dich that bai! Kiem tra JDK ^(can Java 11 tro len^).
    del sources.txt
    pause
    exit /b 1
)
del sources.txt
echo [2/2] Khoi dong web server tai http://localhost:8080
start "" http://localhost:8080
java -Dfile.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.web.ApiServer 8080 web
pause
