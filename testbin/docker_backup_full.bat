@echo off
@echo ===================================
@echo Enter user:     %musicbox_usr%
@echo Enter password: %musicbox_pwd%
@echo ======================================
@echo.
@echo Backup full sql content, schema, db constructs
@echo on
pg_dump -h localhost -p 5434 -U %musicbox_usr% -d musicbox -f musicbox_full.sql
