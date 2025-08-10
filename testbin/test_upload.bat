curl -v -X POST http://localhost:8080/api/songs/upload?storageType=LOCAL ^
  -H "Authorization: Bearer %jwt_token%" ^
  -F "file=@D:\chris\mymusic\uploadSource\BreakStuff.m4a"


	