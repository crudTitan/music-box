package com.boxclone.musicbox.repository;




import com.boxclone.musicbox.entity.AlbumEntity;
import com.boxclone.musicbox.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlbumRepository extends JpaRepository<AlbumEntity, Long> {

    List<AlbumEntity> findAllByUser(UserEntity user);

    Optional<AlbumEntity> findByTitleAndArtist(String album, String albumArtist);
}
