-- DELETE ORDER: children first, then parents
DELETE FROM playlist_songs;
DELETE FROM playlists;
DELETE FROM songs;
DELETE FROM albums;
DELETE FROM user_roles;
DELETE FROM users;
DELETE FROM roles;



INSERT INTO roles (id, name) VALUES 
  (1, 'ROLE_USER'),
  (2, 'ROLE_ADMIN');


INSERT INTO users (id, username, password) VALUES
  (1, 'christopher.j.rood@gmail.com', '$2a$10$xQ6OEQbCEXr/8lKt3gCD8esWt0E2l6KC96lilLJNiP32Z5NP7DrZi'),
  (2, 'abe.lincoln@aol.com',          '$2a$10$BugVTrbKcV9feqX/iOAoT.Y8Suq870u7xuKTZL4PU0BRWxdoSP20.'),
  (3, 'john.lennon@yahoo.com',        '$2a$10$/CLTdQ.HyISm4aef.B7W5udUxeaaibrUg90TaqNl3VO9x5Ub0TiNC');
  
  
INSERT INTO user_roles (user_id, role_id) VALUES
  (1, 1),  -- testuser1 → ROLE_USER
  (2, 1),  -- testuser2 → ROLE_USER
  (3, 1),  -- testadmin3 → ROLE_ADMIN  
  (3, 2);  -- testadmin3 → ROLE_ADMIN  

SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('roles_id_seq', (SELECT MAX(id) FROM roles));



INSERT INTO albums (id, title, artist, release_date, user_id) VALUES
  (1, 'The Dark Side of the Moon', 'Pink Floyd', '1973-03-01', 1),
  (2, 'Abbey Road', 'The Beatles', '1969-09-26', 1),
  (3, 'Choclate Starfish', 'Limp Bizkit', '1994-10-26', 2);

SELECT setval('albums_id_seq', (SELECT MAX(id) FROM albums));


INSERT INTO songs (id, title, artist, duration, storage_type, user_id, album_id) VALUES
  (1, 'Isnt it a pity', 'Beatles', 90, 'LOCAL', 1, 2),	
  (2, 'Come Together', 'Beatles', 259, 'LOCAL', 2, 2),
  (3, 'Time', 'Pink Floyd', 412, 'LOCAL', 3, 1);

SELECT setval('songs_id_seq', (SELECT MAX(id) FROM songs));


INSERT INTO playlists (id, name, user_id) VALUES
  (1, 'Chill Vibes', 1),
  (2, 'Rock Classics', 2);

SELECT setval('playlists_id_seq', (SELECT MAX(id) FROM playlists));


INSERT INTO playlist_songs (playlist_id, song_id) VALUES
  (1, 1),  -- Chill Vibes → Speak to Me
  (2, 2),  -- Rock Classics → Come Together
  (2, 3);  -- Rock Classics → Something


