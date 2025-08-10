curl -v -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"abe.lincoln@aol.com\", \"password\":\"changeme2\"}"


@echo off
rem   -d "{\"username\":\"testuser\", \"password\":\"testpass\"}"

rem   -d "{\"username\":\"testuser\", \"password\":\"oopsWrngPwd\"}"

rem   -d "{\"username\":\"testuser\", \"password\":\"testpassOoopsWrongPwd\"}"

rem 	-d "{\"username\":\"testuser\", \"password\":\"testpass\"}"
