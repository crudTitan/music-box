package com.boxclone.musicbox.config;

import com.boxclone.musicbox.service.Impl.LocalFileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;

public class MboxUtility {

    private static final Logger log = LoggerFactory.getLogger(MboxUtility.class);

    public static String getFileExtension(String filePath, boolean validate) throws FileNotFoundException {

        int dotIndex = filePath.lastIndexOf('.');
        String extension = (dotIndex == -1) ? "" : filePath.substring(dotIndex + 1);
        if (validate) {
            String msg = "Unsupported file format: " + extension;
            if (!"mp3".equals(extension) && !"m4a".equals(extension)) {
                log.error(msg);
                if ("m4p".equals(extension))
                    throw new FileNotFoundException(msg + " Upload failed: DRM-protected .m4p files are not supported. Please use DRM-free formats like .mp3 or .m4a.");
                throw new FileNotFoundException(msg);
            }
        }
        return extension;
    }


    public static String getContentType(String extension) {
        if ("mp3".equals(extension)) {
            return "audio/mpeg";
        } else if ("m4a".equals(extension)) {
            return "audio/mp4";
        } else if ("m4p".equals(extension)) {
            return "audio/mp4";
        } else {
            return "application/octet-stream";
        }
    }
}
