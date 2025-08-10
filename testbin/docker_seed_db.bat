docker exec -i musicbox-postgres psql -U %musicbox_usr% -d musicbox < seed.sql
