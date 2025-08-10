package com.boxclone.musicbox.repository;


import com.boxclone.musicbox.entity.SongEntity;
import com.boxclone.musicbox.entity.StorageType;
import com.boxclone.musicbox.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<SongEntity, Long> {

    List<SongEntity> findAllByUser(UserEntity user);

    // OR if you prefer by ID directly:
    List<SongEntity> findAllByUser_Id(Long userId);

    Optional<SongEntity> findByTitleAndArtistAndUserIdAndStorageType(String albumTitle, String albumArtist, Long id, StorageType storageType);
}
