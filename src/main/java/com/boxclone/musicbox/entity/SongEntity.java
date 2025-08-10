package com.boxclone.musicbox.entity;

import com.boxclone.musicbox.dto.SongDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Entity
@Table(name = "songs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SongEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String artist;
    private int duration;
    private String releaseDate;

    @Enumerated(EnumType.STRING)
    private StorageType storageType;
    private String storageLocationPath;
    private Long storageSize;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "album_id", nullable = true)
    private AlbumEntity album;

    public SongDto toDto() {
        return SongDto.builder()
                .id(getId())
                .title(getTitle())
                .artist(getArtist())
                .duration(getDuration())
                .releaseDate(getReleaseDate())
                .albumTitle(getAlbum().getTitle())
                .storageLocationPath(getStorageLocationPath())
                .storageSize(getStorageSize())
                .storageType(getStorageType())
                .build();
    }
}
