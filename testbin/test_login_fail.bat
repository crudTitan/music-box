curl -v -X POST http://localhost:8080/api/auth/login ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"bogus@aol.com\", \"password\":\"badpass\"}"



