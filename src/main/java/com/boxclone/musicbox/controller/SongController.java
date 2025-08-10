package com.boxclone.musicbox.controller;

import com.boxclone.musicbox.config.EnumValidator;
import com.boxclone.musicbox.config.StorageProperties;
import com.boxclone.musicbox.dto.SongDto;
import com.boxclone.musicbox.entity.SongEntity;
import com.boxclone.musicbox.entity.StorageType;
import com.boxclone.musicbox.repository.FileMetadataRepository;
import com.boxclone.musicbox.service.FileStorageService;
import com.boxclone.musicbox.service.Impl.LocalFileStorageService;
import com.boxclone.musicbox.service.Impl.S3FileStorageService;
import com.boxclone.musicbox.service.SongService;
import com.boxclone.musicbox.service.SongStreamService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/songs")
@RequiredArgsConstructor
@CrossOrigin
@Validated
public class SongController {

    private final SongService songService;
    private final FileMetadataRepository fileMetadataRepo;
    private static final Logger log = LoggerFactory.getLogger(SongController.class);

    @Autowired
    private LocalFileStorageService localStorage;
    @Autowired
    private S3FileStorageService s3Storage;
    @Autowired
    private StorageProperties storageProperties;
    @Autowired
    private SongStreamService songStreamService;

    @GetMapping("/users")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<SongDto>> getUserSongs(Authentication authentication) {
        log.debug("Enter getUserSongs: {}", Optional.ofNullable(authentication)
                .map(Authentication::getName).orElse("AnonymousNullUser"));

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        }

        List<SongEntity> songs = songService.getAllSongsForUser(authentication.getName());

        List<SongDto> result = songs.stream()
                .map(song -> SongDto.builder().
                        id(song.getId()).
                        title(song.getTitle()).
                        artist(song.getArtist()).
                        duration(song.getDuration()).
                        albumTitle(song.getAlbum().getTitle()).
                        storageType(song.getStorageType()).
                        storageLocationPath(song.getStorageLocationPath()).
                        storageSize(song.getStorageSize()).
                        build()
                )
                .toList();

        log.debug("Exit getUserSongs: user:{} songCount:{}", Optional.ofNullable(authentication)
                .map(Authentication::getName).orElse("AnonymousNullUser"), result.size() );

        return ResponseEntity.ok(result);
    }


    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SongDto>> getSongs() {
        log.debug("Enter getSongs: all");

        List<SongEntity> songsFound = songService.getAllSongs();

        List<SongDto> response = songsFound.stream()
                .map(song -> SongDto.builder().
                                id(song.getId()).
                                title(song.getTitle()).
                                artist(song.getArtist()).
                                duration(song.getDuration()).
                                albumTitle(song.getAlbum().getTitle()).
                                storageType(song.getStorageType()).
                                storageLocationPath(song.getStorageLocationPath()).
                                storageSize(song.getStorageSize()).
                                build()
                        )
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<String> uploadSong(@RequestParam("file") @NotNull MultipartFile file,
                                             @RequestParam(defaultValue = "LOCAL") @NotNull @EnumValidator(enumClass = StorageType.class) String storageType,
                                             @NotNull Authentication auth ) throws IOException {

        log.debug("Enter uploadSong, file: {}, fileSz: {} authUser: {} storageType:[{}-{}]",
                file.getOriginalFilename(), file.getSize(), auth.getName(), storageType, storageProperties.getLocation());

        StorageType storageTypeEnum = StorageType.fromString(storageType);
        FileStorageService selectedStorage = StorageType.S3.equals(storageTypeEnum)
                ? s3Storage : localStorage;
        String usernameEmail = auth.getName();
        String filename = file.getOriginalFilename();

        SongDto songDto = selectedStorage.store(file, usernameEmail);
        if (null == songDto) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store file null song");
        }

        log.debug("Exited uploadSong, with success!!, file: {}, fileSz: {} authUser: {} storageType:{}",
                file.getName(), file.getSize(), auth.getName(), storageType);

        return ResponseEntity.ok("Uploaded " + filename + " for " + usernameEmail);
    }



    @GetMapping("/stream/{filename}")
    @PreAuthorize("hasRole('USER')")
    public void stream(@PathVariable String filename,
                       @RequestParam(defaultValue = "LOCAL") String storageType,
                       Authentication auth,
                       HttpServletResponse response) throws IOException {

        response.setContentType("audio/mpeg");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        songStreamService.streamSong(filename, auth.getName(), response, storageType);
    }



}


