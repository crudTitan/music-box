package com.boxclone.musicbox.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/storage")
@CrossOrigin
public class StorageController {

    @GetMapping("/types")
    @PreAuthorize("hasRole('USER')")
    public List<StorageTypeDto> getStorageTypes() {
        return List.of(
                new StorageTypeDto("local", "Local Storage"),
                new StorageTypeDto("s3", "Amazon S3")
        );
    }

    public static record StorageTypeDto(String id, String name) {}
}

