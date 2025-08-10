package com.boxclone.musicbox.repository;

import com.boxclone.musicbox.entity.FileMetadataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileMetadataRepository extends JpaRepository<FileMetadataEntity, Long> {
    List<FileMetadataEntity> findByUsernameEmail(String email);
}
