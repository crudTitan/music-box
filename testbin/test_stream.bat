del BreakStuff_downloaded.m4a*
dir BreakStuff_downloaded.*

echo jwt_token: %jwt_token%

curl -v ^
  -X GET "http://localhost:8080/api/songs/stream/BreakStuff.m4a?sourceStorageType=S3" ^
  -H "Authorization: Bearer %jwt_token%" ^
  --output BreakStuff_downloaded.m4a


dir BreakStuff_downloaded.*	