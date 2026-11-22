@echo off
chcp 65001 > nul
title DEMO - Phan tich mang xa hoi bang do thi (dong cua so nay de tat server)
cd /d "%~dp0"

echo ===============================================================================
echo   BAI TAP LON: PHAN TICH MANG XA HOI BANG DO THI (JAVA) - CHE DO DEMO
echo ===============================================================================

REM Tat server Java cu dang chiem cong 8080 (neu lan truoc chua tat)
powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | ForEach-Object { $p = Get-Process -Id $_.OwningProcess -ErrorAction SilentlyContinue; if ($p -and $p.ProcessName -like 'java*') { Stop-Process -Id $p.Id -Force } }"

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
echo       Trinh duyet se tu mo va TU DONG DEMO tat ca chuc nang.
echo       Phim tat tren trang: Space = dung/tiep, mui ten = qua buoc, Esc = thoat demo.
echo       De TAT demo: dong cua so nay.
echo.

REM Cho server san sang roi moi mo trinh duyet
start "" powershell -NoProfile -WindowStyle Hidden -Command "for($i=0;$i -lt 60;$i++){ try { Invoke-WebRequest 'http://localhost:8080/api/graph' -UseBasicParsing -TimeoutSec 1 | Out-Null; break } catch { Start-Sleep -Milliseconds 500 } }; Start-Process 'http://localhost:8080/?demo=auto'"

java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.web.ApiServer 8080 web
pause
