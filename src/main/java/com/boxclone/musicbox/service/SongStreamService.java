package com.boxclone.musicbox.service;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface SongStreamService {

    void streamSong(String filename, String authName,
                           HttpServletResponse response, String sourceStorageType) throws IOException;
}
