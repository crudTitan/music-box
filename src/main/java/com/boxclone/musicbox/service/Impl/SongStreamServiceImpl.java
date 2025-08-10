package com.boxclone.musicbox.service.Impl;

import com.boxclone.musicbox.service.SongStreamService;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class SongStreamServiceImpl implements SongStreamService {


    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public void streamSong(String filename, String authName,
                           HttpServletResponse response, String sourceStorageType) throws IOException {
        if ("S3".equalsIgnoreCase(sourceStorageType)) {
            streamFromS3(filename, authName, response);
        } else {
            streamFromLocal(filename, authName, response);
        }
    }

    public void streamFromLocal(String filename, String authName, HttpServletResponse response) throws IOException {
        Path filePath = Paths.get("uploads", authName, filename);
        if (!Files.exists(filePath)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(Files.probeContentType(filePath));
        response.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");

        try (InputStream in = Files.newInputStream(filePath);
             OutputStream out = response.getOutputStream()) {
            in.transferTo(out);
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

