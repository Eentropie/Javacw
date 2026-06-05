@echo off
setlocal
title JavaCW Desktop Launcher

cd /d "%~dp0"

echo Compiling JavaCW...
if not exist out mkdir out

set "SOURCE_LIST=%TEMP%\javacw_sources_%RANDOM%.txt"
if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"
for /r "src" %%F in (*.java) do echo "%%F">>"%SOURCE_LIST%"

javac -d out @"%SOURCE_LIST%"
if errorlevel 1 (
  echo.
  echo Compilation failed. Check the errors above.
  if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"
  pause
  exit /b 1
)

if exist "%SOURCE_LIST%" del "%SOURCE_LIST%"

echo.
echo Starting JavaCW desktop app...
java -cp out gui.DesktopMain

echo.
echo JavaCW desktop app closed.
pause
