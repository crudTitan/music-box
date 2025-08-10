package com.boxclone.musicbox.entity;

import org.springframework.http.ResponseEntity;

public enum StorageType {
    LOCAL,
    S3;

    public static StorageType fromString(String storageTypeName) {
        StorageType storageTypeEnum;
        try {
            storageTypeEnum = StorageType.valueOf(storageTypeName);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return storageTypeEnum;
    }
}
