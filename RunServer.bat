@echo off
title MMORPG SERVER - PORTABLE MODE
color 0A
cd /d "%~dp0"

set MVN_INTERNAL=".\maven\bin\mvn.cmd"

echo =====================
echo   DANG CHAY SERVER
echo =====================

if exist %MVN_INTERNAL% (
    call %MVN_INTERNAL% exec:java -Dexec.mainClass="com.mmorpg.server.MainServer"
) else (
    echo [LOI] Khong tim thay thu muc maven!
)

pause