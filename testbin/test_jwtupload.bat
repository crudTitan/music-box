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

REM 2) Upload using curl with the token
curl -v -X POST "http://localhost:8080/api/songs/upload?storageType=LOCALLOCO" ^
  -H "Authorization: Bearer !JWT_TOKEN!" ^
  -F "file=@D:\chris\mymusic\uploadSource\BreakStuff.m4a"

endlocal
pause
