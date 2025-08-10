package com.boxclone.musicbox.service.Impl;

import com.boxclone.musicbox.entity.SongEntity;
import com.boxclone.musicbox.entity.UserEntity;
import com.boxclone.musicbox.repository.AlbumRepository;
import com.boxclone.musicbox.repository.SongRepository;
import com.boxclone.musicbox.repository.UserRepository;
import com.boxclone.musicbox.service.SongService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@AllArgsConstructor
public class SongServiceImpl implements SongService {

    SongRepository songRepository;
    AlbumRepository albumRepository;
    UserRepository userRepository;

    public void addSong() {
        System.out.println("add song");
    }
    public void deleteSong() {
        System.out.println("delete song");
    }
    public void updateSong() {
        System.out.println("update song");
    }
    public List<SongEntity> getAllSongs() {
        System.out.println("get all songs");
        return songRepository.findAll();
    }

    @Override
    public List<SongEntity> getAllSongsForUser(String userNameEmail) {

        UserEntity user = userRepository.findByUsername(userNameEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return songRepository.findAllByUser(user);
    }
}
