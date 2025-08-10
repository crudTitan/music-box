package com.boxclone.musicbox.exceptions;


public class StorageQuotaExceededException extends RuntimeException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
