curl -v -X POST http://localhost:8080/api/songs/upload?storageType=LOCAL ^
  -H "Authorization: Bearer %jwt_token%" ^
  -F "file=@"%musicbox_upload_src%\21IsntItaPityLive.m4p""


	