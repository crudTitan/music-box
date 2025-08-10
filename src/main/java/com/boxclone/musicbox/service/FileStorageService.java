package com.boxclone.musicbox.service;

import com.boxclone.musicbox.dto.SongDto;
import com.boxclone.musicbox.entity.StorageType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    SongDto store(MultipartFile file, String usernameEmail) throws IOException;
}