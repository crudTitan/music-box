package com.boxclone.musicbox.dto;


import com.boxclone.musicbox.entity.AlbumEntity;
import com.boxclone.musicbox.entity.SongEntity;
import com.boxclone.musicbox.entity.StorageType;
import com.boxclone.musicbox.entity.UserEntity;
import lombok.Builder;
import lombok.Data;

//@Data
//Generates getters for all fields,
// a useful toString method, and hashCode and equals implementations that check all non-transient fields.
// Will also generate setters for all non-final fields, as well as a
// constructor (except that no constructor will be generated if any
// explicitly written constructors already exist)

@Data
@Builder
public class SongDto {
    private Long id;

    private int duration;
    private String title;
    private String artist;
    private String albumTitle;
    private String releaseDate;

    private String storageLocationPath;
    private StorageType storageType;
    private Long storageSize;

    private String usernameEmail;

    public SongEntity toEntity(UserEntity user, AlbumEntity album) {
        return SongEntity.builder().title(getTitle()).
                                    artist(getArtist()).
                                    duration(getDuration()).
                                    storageLocationPath(getStorageLocationPath()).
                                    storageSize(getStorageSize()).
                                    storageType(getStorageType()).
                                    user(user).
                                    album(album).
                                    build();
    }
}