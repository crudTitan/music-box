package com.boxclone.musicbox.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


import java.time.LocalDateTime;

@Entity
@Table(name = "file_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadataEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String usernameEmail;
    private String storgeLocationPath;
    private StorageType storageType;
    private String contentType;
    private long storageSize;
    private LocalDateTime uploadedAt;

    // Add this custom constructor manually
    public FileMetadataEntity(String usernameEmail, String storgeLocationPath, StorageType storageType, long storageSize, String contentType,  LocalDateTime uploadedAt) {
        this.usernameEmail = usernameEmail;
        this.storgeLocationPath = storgeLocationPath;
        this.storageType = storageType;
        this.storageSize = storageSize;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
    }

}
