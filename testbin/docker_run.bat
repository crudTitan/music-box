@echo on

@echo ===================================
@echo Enter user:     %musicbox_usr%
@echo Enter password: %musicbox_pwd%
@echo ===================================
@echo.

docker run --name musicbox-postgres    ^
  -e POSTGRES_DB=musicbox              ^
  -e POSTGRES_USER=%musicbox_usr%      ^
  -e POSTGRES_PASSWORD=%musicbox_pwd%  ^
  -p 5434:5432                         ^
  -v pgdata:/var/lib/postgresql/data   ^
  -d postgres:15

@echo off

