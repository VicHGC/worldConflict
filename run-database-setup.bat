@echo off
REM Script to create tables in MySQL database
REM Run this from command prompt or PowerShell

echo Creating tables in MySQL...

mysql -u root -p1234 worldConflict_db < "%~dp0database-setup.sql"

pause