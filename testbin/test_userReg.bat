curl -v -X POST http://localhost:8080/api/auth/register ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"testuser\", \"password\":\"testpass\"}"
