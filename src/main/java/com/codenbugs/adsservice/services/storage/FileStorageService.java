package com.codenbugs.adsservice.services.storage;

import com.codenbugs.adsservice.exceptions.FileStorageException;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    String uploadFile(MultipartFile file, String folder) throws FileStorageException;

    void deleteFile(String fileUrl) throws FileStorageException;
}
