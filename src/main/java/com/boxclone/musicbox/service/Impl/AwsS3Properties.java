package com.boxclone.musicbox.service.Impl;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "aws.s3")
@Data
public class AwsS3Properties {
    private String bucketName;
    private String region;
    private String accessKey;
    private String secretKey;
}