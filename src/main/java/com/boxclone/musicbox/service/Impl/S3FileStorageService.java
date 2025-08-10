package com.boxclone.musicbox.service.Impl;

import com.boxclone.musicbox.dto.SongDto;
import com.boxclone.musicbox.entity.StorageType;
import com.boxclone.musicbox.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetBucketLocationRequest;
import software.amazon.awssdk.services.s3.model.GetBucketLocationResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;


@Component
@RequiredArgsConstructor
public class S3FileStorageService implements FileStorageService {

    private final AwsS3Properties awsS3Properties;
    private static final Logger log = LoggerFactory.getLogger(S3FileStorageService.class);

    private S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(awsS3Properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(
                                awsS3Properties.getAccessKey(),
                                awsS3Properties.getSecretKey())))
                .build();
    }

    private S3Client s3ClientFromDefault() {
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build(); // No region specified; auto-detect from AWS config
    }

    private S3Client s3ClientFromNamedProfile() {
        return S3Client.builder()
                .credentialsProvider(ProfileCredentialsProvider.create("musicbox"))
                .build();
    }

    @Override
    public SongDto store(MultipartFile file, String userEmail) throws IOException {
        S3Client s3 = s3ClientFromDefault();
        String key = userEmail + "/" + file.getOriginalFilename();

        // 🔍 Get bucket region/location
        GetBucketLocationResponse locationResponse = s3.getBucketLocation(
                GetBucketLocationRequest.builder()
                        .bucket(awsS3Properties.getBucketName())
                        .build()
        );

        // Note: AWS may return null for us-east-1, so fall back to default
        String bucketRegion = locationResponse.locationConstraintAsString();
        if (bucketRegion == null || bucketRegion.isEmpty()) {
            bucketRegion = "us-east-1";
        }

        log.debug( "AWS S3 store: now uploading to key:{} bucket:{} region:{}",
                key, awsS3Properties.getBucketName(), bucketRegion);

        PutObjectResponse resp = s3.putObject(PutObjectRequest.builder()
                        .bucket(awsS3Properties.getBucketName())
                        .key(key)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.debug( "AWS S3 store: upload no error!!! to key:{} bucket:{} region:{} resp:{}",
                key, awsS3Properties.getBucketName(), bucketRegion, resp.toString());

        return SongDto.builder().duration(0).build(); //TODO: ADD THIS FOR S3
    }
}