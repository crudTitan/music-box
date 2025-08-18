package com.boxclone.musicbox.service.Impl;

import com.boxclone.musicbox.config.MboxUtility;
import com.boxclone.musicbox.config.StorageProperties;
import com.boxclone.musicbox.dto.SongDto;
import com.boxclone.musicbox.entity.AlbumEntity;
import com.boxclone.musicbox.entity.SongEntity;
import com.boxclone.musicbox.entity.StorageType;
import com.boxclone.musicbox.entity.UserEntity;
import com.boxclone.musicbox.exceptions.StorageQuotaExceededException;
import com.boxclone.musicbox.repository.AlbumRepository;
import com.boxclone.musicbox.repository.SongRepository;
import com.boxclone.musicbox.repository.UserRepository;
import com.boxclone.musicbox.service.FileStorageService;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.Tag;


@Component
public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);

    @Autowired
    private StorageProperties storageProperties;

    @Autowired
    private SongRepository songRepository;
    @Autowired
    private AlbumRepository albumRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public SongDto store(MultipartFile file, String userEmail) throws IOException {

        Path uploadPathDir = Paths.get("uploads", userEmail);
        Path uploadPath = uploadPathDir.resolve(file.getOriginalFilename());
        Files.createDirectories(uploadPathDir);

        log.debug("Uploading file: {} to path: {}", file.getOriginalFilename(), uploadPath.toString());
        Optional<UserEntity> userEntity = userRepository.findByUsername(userEmail);

        checkQuotaExceeded(uploadPathDir, file);

        SongDto songDto = null;
        File targetFile = null;
        String uploadAbsolutePath = uploadPath.toAbsolutePath().toString();
        Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);

        try {
            Files.copy(file.getInputStream(), uploadPath, StandardCopyOption.REPLACE_EXISTING);
            targetFile = uploadPath.toFile();

            log.debug("Uploaded file: {} to path: {}", file.getOriginalFilename(), uploadPath.resolve(file.getOriginalFilename()));

            SongEntity songEntity = extractSongDtoFromMetadata(targetFile, StorageType.LOCAL, userEntity.orElse(null));

            if (songEntity != null) {
                songRepository.save(songEntity);
                songDto =  songEntity.toDto();
            } else {
                log.error("Error extracting song metadata from file: {}", uploadAbsolutePath);
                throw new IOException("Error extracting song metadata from file: " + uploadAbsolutePath);
            }
        } catch (Exception e) {
            removeUploadedFile( targetFile, uploadAbsolutePath );
            log.error("Error uploading file: {}, error:{}", uploadAbsolutePath, e.getMessage());
            throw new IOException("Error uploading file: " + file.getOriginalFilename() +  " error: " + e.getMessage(), e);
        }

        return songDto;
    }

    private void removeUploadedFile(File targetFile, String uploadAbsolutePath) {
        try {
            if (null != targetFile && !targetFile.delete()) {
                log.error("Error deleting uploaded file: {}", uploadAbsolutePath);
            }
        } catch (Exception ex) {
            log.error("Error deleting uploaded file:{}, error:{}", uploadAbsolutePath, ex.getMessage());
        }
    }

    private SongEntity extractSongDtoFromMetadata(File targetFile, StorageType storageType, UserEntity userEntity) throws CannotReadException, TagException, InvalidAudioFrameException, ReadOnlyFileException, IOException {

        try {
            AudioFile audioFile = AudioFileIO.read(targetFile);

            log.debug("Reading metadata from path:{} absPath:{} canonPath:{} totalSpace:{} len:{}",
                    targetFile.getPath(), targetFile.getAbsolutePath(), targetFile.getCanonicalPath(),
                    targetFile.getTotalSpace(), targetFile.length()  );

            String filePath = targetFile.getPath();
            String extension = MboxUtility.getFileExtension( filePath, true );
            Tag tag = audioFile.getTag();

            String title = tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE);
            String artist = tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST);
            String albumTitle = tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM);
            String releaseDate = tag.getFirst(org.jaudiotagger.tag.FieldKey.YEAR);
            String albumArtist = tag.getFirst(org.jaudiotagger.tag.FieldKey.ALBUM_ARTIST);
            String genre = tag.getFirst(org.jaudiotagger.tag.FieldKey.GENRE);
            String originalYear = tag.getFirst(org.jaudiotagger.tag.FieldKey.ORIGINAL_YEAR);
            originalYear = Objects.isNull(originalYear) || originalYear.isEmpty() ?
                    releaseDate : originalYear;
            String trackNumber = tag.getFirst(org.jaudiotagger.tag.FieldKey.TRACK);
            int duration = audioFile.getAudioHeader().getTrackLength();

            if (title.isBlank() && artist.isBlank()) {
                throw new FileNotFoundException("Failed to read song metadata, appears to be unsupported file format: " + extension);
            }

            Optional<AlbumEntity> albumEntity = albumRepository.findByTitleAndArtist(albumTitle, albumArtist);
            if (albumEntity.isEmpty()) {
                log.debug("No album found for title:{} artist:{} releaseDate:{} songDate:{} user:{}",
                        albumTitle, albumArtist, releaseDate, originalYear, userEntity.getUsername()  );

                AlbumEntity album = AlbumEntity.builder().
                                        artist(albumArtist).
                                        title(albumTitle).
                                        releaseDate(convertToLocalDate(releaseDate)).
                                        user(userEntity).
                                        build();

                AlbumEntity savedAlbum = albumRepository.save(album);
                albumEntity = Optional.of(savedAlbum);

                log.debug("Album saved for title:{} artist:{} user:{} albumId:{}",
                        savedAlbum.getTitle(), savedAlbum.getArtist(), savedAlbum.getUser().getUsername(), savedAlbum.getId());
            }
            Optional<SongEntity> songEntityExist = songRepository.findByTitleAndArtistAndUserIdAndStorageType(title, artist, userEntity.getId(), storageType);
            return SongEntity.builder()
                    .id(songEntityExist.map(SongEntity::getId).orElse(null))
                    .title(title)
                    .artist(artist)
                    .duration(duration)
                    .releaseDate(originalYear)
                    .storageLocationPath(targetFile.getPath())
                    .storageType(storageType)
                    .storageSize(targetFile.getTotalSpace())
                    .user(userEntity)
                    .album(albumEntity.orElseGet(() -> AlbumEntity.builder().build()))
                    .build();

        } catch (Exception e) {
            log.error("Error reading audio metadata: ", e);
           throw e;
        }

    }

    private LocalDate convertToLocalDate(String releaseDate) {
        try {
            // Parse the string into an OffsetDateTime (date-time with zone offset)
            OffsetDateTime odt = OffsetDateTime.parse(releaseDate, DateTimeFormatter.ISO_OFFSET_DATE_TIME);

            // Extract LocalDate from the OffsetDateTime
            LocalDate localDate = odt.toLocalDate();

            log.debug( "Converted song meta release date:{} to localDate:{}", releaseDate, localDate);  // Output: 1992-09-29
            return localDate;
        } catch (Exception e) {
            log.error("Error converting releaseDate:{} to LocalDate: ", releaseDate, e);
            return LocalDate.now();
        }
    }

    /// TODO: Make this common utility, maybe storage util or something?
    private void checkQuotaExceeded(Path uploadPath, MultipartFile file) throws IOException {
        long currentUsage = Files.walk(uploadPath)
                .filter(Files::isRegularFile)
                .mapToLong(path -> path.toFile().length())
                .sum();

        long newFileSize = file.getSize();
        long userQuotaBytes = storageProperties.getUserQuotaBytes();

        if (currentUsage + newFileSize > userQuotaBytes) {
            throw new StorageQuotaExceededException("User exceeded 500MB quota.");
        }
    }
}
