package com.boxclone.musicbox.config;

import com.boxclone.musicbox.entity.StorageType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
@Data
public class StorageProperties {
    private long userQuotaBytes;

    public StorageType location;
}
