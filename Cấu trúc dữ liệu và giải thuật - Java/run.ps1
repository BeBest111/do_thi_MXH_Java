# PowerShell script: Bien dich va chay chuong trinh Phan tich mang xa hoi bang do thi
$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
if ($PSScriptRoot) {
    Set-Location -Path $PSScriptRoot
}

[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Console]::InputEncoding  = [System.Text.Encoding]::UTF8
$OutputEncoding           = [System.Text.Encoding]::UTF8

Write-Host "===============================================================================" -ForegroundColor Cyan
Write-Host "  HOC VIEN CONG NGHE BUU CHINH VIEN THONG - KHOA CONG NGHE THONG TIN" -ForegroundColor Cyan
Write-Host "  BAI TAP LON: PHAN TICH MANG XA HOI BANG DO THI (JAVA)" -ForegroundColor Cyan
Write-Host "===============================================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) {
    New-Item -ItemType Directory -Path "bin" | Out-Null
}

Write-Host "[1/2] Dang bien dich ma nguon Java..." -ForegroundColor Yellow

$sources = @(
    "src/ptit/dsa/socialnetwork/model/User.java",
    "src/ptit/dsa/socialnetwork/graph/SocialGraph.java",
    "src/ptit/dsa/socialnetwork/analysis/FriendRecommendation.java",
    "src/ptit/dsa/socialnetwork/analysis/ConnectionPath.java",
    "src/ptit/dsa/socialnetwork/analysis/SocialAnalyzer.java",
    "src/ptit/dsa/socialnetwork/app/SocialNetworkApp.java",
    "src/ptit/dsa/socialnetwork/Main.java",
    "src/ptit/dsa/socialnetwork/test/SocialNetworkTest.java"
)

$sourcesFile = "sources.txt"
$sources | Out-File -FilePath $sourcesFile -Encoding ascii

javac -encoding UTF-8 -d bin "@$sourcesFile"
$compileStatus = $LASTEXITCODE

if (Test-Path $sourcesFile) {
    Remove-Item $sourcesFile -Force
}

if ($compileStatus -ne 0) {
    Write-Host "[LOI] Bien dich that bai! Vui long kiem tra JDK Java tren may." -ForegroundColor Red
    Read-Host "Nhan Enter de thoat..."
    exit $compileStatus
}

Write-Host "[2/2] Bien dich thanh cong!" -ForegroundColor Green
Write-Host ""
Write-Host "=============================== CHON CHE DO ===============================" -ForegroundColor Cyan
Write-Host "1. Chay ung dung Menu tuong tac (Main - SocialNetworkApp)"
Write-Host "2. Chay bo kiem thu tu dong toan dien (SocialNetworkTest)"
Write-Host "===========================================================================" -ForegroundColor Cyan
$mode = Read-Host "Nhap lua chon cua ban (1 hoac 2, mac dinh 1)"

if ($mode -eq "2") {
    java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.test.SocialNetworkTest
} else {
    java -Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8 -cp bin ptit.dsa.socialnetwork.Main
}

Read-Host "`nNhan Enter de ket thuc..."
