package com.boxclone.musicbox.service.Impl;

import com.boxclone.musicbox.dto.SongDto;
import com.boxclone.musicbox.entity.SongEntity;
import com.boxclone.musicbox.entity.StorageType;
import com.boxclone.musicbox.repository.SongRepository;
import com.boxclone.musicbox.service.SongStreamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SongStreamServiceImpl implements SongStreamService {


    private final SongRepository songRepository;
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

//    // TODO: this may be obsolete?
//    public void streamSong(String filename, String authName,
//                           HttpServletResponse response, String sourceStorageType) throws IOException {
//        if ("S3".equalsIgnoreCase(sourceStorageType)) {
//            streamFromS3(filename, authName, response);
//        } else {
//            streamFromLocal(filename, authName, response);
//        }
//    }

    @Override
    public SongDto streamSong(Long id, String name,
                              HttpServletRequest request,
                              HttpServletResponse response) throws IOException {
        Optional<SongEntity> song = songRepository.findById(id);
        if (song.isEmpty()) {
            throw new IOException("Failed to find song with id: " + id);
        }

        if (StorageType.S3.equals(song.get().getStorageType())) {
            streamFromS3(song.get().getStorageLocationPath(), song.get().getArtist(), response);
        } else {
            streamFromLocal(song.get().getStorageLocationPath(), song.get().getArtist(), request, response);
        }

        return song.get().toDto(); // TODO: use get mapping?
    }

    public void streamFromLocal(String filename, String artistName,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        Path filePath = Paths.get(filename);
        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        long fileLength = Files.size(filePath);
        String rangeHeader = request.getHeader("Range");
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
        response.setContentType(mimeType);

        try (RandomAccessFile randomFile = new RandomAccessFile(filePath.toFile(), "r");
             OutputStream out = response.getOutputStream()) {

            if (rangeHeader != null) {
                // Example: "Range: bytes=500-999"
                long start = 0, end = fileLength - 1;
                String[] ranges = rangeHeader.replace("bytes=", "").split("-");
                if (!ranges[0].isEmpty()) start = Long.parseLong(ranges[0]);
                if (ranges.length > 1 && !ranges[1].isEmpty()) end = Long.parseLong(ranges[1]);

                long contentLength = end - start + 1;
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader("Content-Length", String.valueOf(contentLength));

                randomFile.seek(start);
                byte[] buffer = new byte[8192];
                long bytesRemaining = contentLength;
                int bytesRead;
                while ((bytesRead = randomFile.read(buffer)) != -1 && bytesRemaining > 0) {
                    out.write(buffer, 0, (int) Math.min(bytesRead, bytesRemaining));
                    bytesRemaining -= bytesRead;
                }
            } else {
                // Full file
                response.setHeader("Content-Length", String.valueOf(fileLength));
                randomFile.seek(0);
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = randomFile.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
        }
    }

    // TODO: move this to common helper, static or?
    private S3Client s3ClientFromDefault() {
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build(); // No region specified; auto-detect from AWS config
    }

    public void streamFromS3(String filename, String authName, HttpServletResponse response) throws IOException {
        String s3Key = authName + "/" + filename;

        S3Client s3Client = s3ClientFromDefault();

        try (ResponseInputStream<?> s3Stream = s3Client.getObject(
                GetObjectRequest.builder()
                        .bucket(bucketName)
                        .key(s3Key)
                        .build()
        )) {
            // We can't always get metadata easily here unless we query first
            response.setContentType("audio/mpeg"); // or detect via filename
            response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

            try (OutputStream out = response.getOutputStream()) {
                s3Stream.transferTo(out);
            }
        } catch (NoSuchKeyException e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}

