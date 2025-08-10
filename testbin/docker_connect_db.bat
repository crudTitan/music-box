@echo off

docker ps
docker volume ls
docker volume inspect pgdata


call show_ports.bat

@echo ===================================
@echo Enter user:     %musicbox_usr%
@echo Enter password: %musicbox_pwd%
@echo ======================================

@echo "to connect to the db"
set PGPASSWORD=%musicbox_pwd%

@echo on
psql -h localhost -p 5434 -U %musicbox_usr% -d musicbox



rem call show_ports.bat
