@echo off
setlocal
set "SCRIPT=%~dp0collect-rustify-debug.ps1"
powershell.exe -NoLogo -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT%" %*
if errorlevel 1 (
  echo.
  echo Collection failed. If you use a custom launcher, drag its instance folder onto this BAT file.
  pause
  exit /b 1
)
pause
