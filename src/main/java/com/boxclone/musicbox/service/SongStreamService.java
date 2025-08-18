package com.boxclone.musicbox.service;

import com.boxclone.musicbox.dto.SongDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface SongStreamService {

//    void streamSong(String filename, String authName,
//                           HttpServletResponse response, String sourceStorageType) throws IOException;

    //SongDto streamSong(Long id, String name, HttpServletResponse response) throws IOException;

    SongDto streamSong(Long id, String name,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException;
}
