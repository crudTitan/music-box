package com.boxclone.musicbox.service;

import com.boxclone.musicbox.entity.SongEntity;

import java.util.List;

public interface SongService {
    public void addSong();
    public void deleteSong();
    public void updateSong();
    public List<SongEntity> getAllSongs();
    public List<SongEntity> getAllSongsForUser(String userNameEmail);
}
