package com.services.file_storage.controller;

import com.services.file_storage.dto.FileDeleteResponse;
import com.services.file_storage.dto.FileUploadResponse;
import com.services.file_storage.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        FileUploadResponse response = fileStorageService.uploadFile(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", required = false) String folder) {
        List<FileUploadResponse> responses = fileStorageService.uploadMultipleFiles(files, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<FileDeleteResponse> deleteFile(@PathVariable String publicId) {
        // Replace forward slashes in path variable
        String decodedPublicId = publicId.replace("_", "/");
        FileDeleteResponse response = fileStorageService.deleteFile(decodedPublicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/url/{publicId}")
    public ResponseEntity<String> getFileUrl(@PathVariable String publicId) {
        String decodedPublicId = publicId.replace("_", "/");
        String url = fileStorageService.getFileUrl(decodedPublicId);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/transform/{publicId}")
    public ResponseEntity<String> getTransformedImageUrl(
            @PathVariable String publicId,
            @RequestParam(defaultValue = "300") int width,
            @RequestParam(defaultValue = "300") int height,
            @RequestParam(defaultValue = "fill") String crop) {
        String decodedPublicId = publicId.replace("_", "/");
        String url = fileStorageService.getTransformedImageUrl(decodedPublicId, width, height, crop);
        return ResponseEntity.ok(url);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("File Storage Service is running");
    }
}
