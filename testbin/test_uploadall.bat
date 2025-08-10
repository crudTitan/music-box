@echo on
setlocal EnableDelayedExpansion

REM 1) Login via PowerShell and capture token
for /f "usebackq delims=" %%T in (`powershell -NoProfile -Command "try { $r = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body '{\"username\":\"abe.lincoln@aol.com\",\"password\":\"changeme2\"}'; Write-Output $r.token } catch { Write-Output ''; exit 1 }"`) do (
  set "JWT_TOKEN=%%~T"
)

if not defined JWT_TOKEN (
  echo ERROR: failed to obtain JWT token.
  pause
  exit /b 1
)

echo Token obtained (first 12 chars): !JWT_TOKEN:~0,12!...

REM 2) Upload all files in the directory
set "UPLOAD_DIR=D:\chris\mymusic\uploadSource"
set "STORAGE_TYPE=LOCAL"

for %%F in ("%UPLOAD_DIR%\*") do (
  echo Uploading file: %%F
  curl -v -X POST "http://localhost:8080/api/songs/upload?storageType=%STORAGE_TYPE%" ^
    -H "Authorization: Bearer !JWT_TOKEN!" ^
    -F "file=@%%F"
  echo.
)

endlocal
pause
