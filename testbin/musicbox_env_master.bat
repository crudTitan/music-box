
set musicbox_usr=<docker postgreSql user login>
set musicbox_pwd=<docker postgreSql user password>
set musicbox_jwt_secret=<JWT secret seed base, random string to base JWT encryption on>
set musicbox_upload_src=<music box directory to upload mp3, mp4, etc. files from generally speaking for curl tests, UI will browse and prompt for music location>


@echo Music box env:
@echo   user:                %musicbox_usr%
@echo   password:            %musicbox_pwd%
@echo   jwt secret:          %musicbox_jwt_secret%
@echo   musicbox_upload_src: %musicbox_upload_src%

