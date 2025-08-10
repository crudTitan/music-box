@echo off
@echo This will restore the database up recreating the container typically
@echo ===================================
@echo Enter user:     %musicbox_usr%
@echo Enter password: %musicbox_pwd%
@echo ======================================
@echo.
@echo Restore full sql content, schema, db constructs
@echo on
docker exec -i musicbox-postgres psql -U %musicbox_usr% -d musicbox < musicbox_full.sql
