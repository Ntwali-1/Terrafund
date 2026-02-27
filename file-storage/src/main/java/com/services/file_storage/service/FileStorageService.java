package com.services.file_storage.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.services.file_storage.dto.FileDeleteResponse;
import com.services.file_storage.dto.FileUploadResponse;
import com.services.file_storage.exception.FileStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final Cloudinary cloudinary;

    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private static final List<String> ALLOWED_DOCUMENT_TYPES = List.of(
            "application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public FileUploadResponse uploadFile(MultipartFile file, String folder) {
        validateFile(file);

        try {
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", folder != null ? folder : "uploads",
                    "resource_type", "auto"
            );

            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);

            log.info("File uploaded successfully: {}", uploadResult.get("public_id"));

            return FileUploadResponse.builder()
                    .publicId((String) uploadResult.get("public_id"))
                    .url((String) uploadResult.get("url"))
                    .secureUrl((String) uploadResult.get("secure_url"))
                    .format((String) uploadResult.get("format"))
                    .resourceType((String) uploadResult.get("resource_type"))
                    .size(((Number) uploadResult.get("bytes")).longValue())
                    .width((Integer) uploadResult.get("width"))
                    .height((Integer) uploadResult.get("height"))
                    .message("File uploaded successfully")
                    .build();

        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage());
            throw new FileStorageException("Failed to upload file: " + e.getMessage(), e);
        }
    }

    public List<FileUploadResponse> uploadMultipleFiles(List<MultipartFile> files, String folder) {
        List<FileUploadResponse> responses = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                FileUploadResponse response = uploadFile(file, folder);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
                responses.add(FileUploadResponse.builder()
                        .message("Failed to upload: " + file.getOriginalFilename())
                        .build());
            }
        }

        return responses;
    }

    public FileDeleteResponse deleteFile(String publicId) {
        try {
            Map deleteResult = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String result = (String) deleteResult.get("result");

            log.info("File deletion result for {}: {}", publicId, result);

            return FileDeleteResponse.builder()
                    .publicId(publicId)
                    .result(result)
                    .message("ok".equals(result) ? "File deleted successfully" : "File not found or already deleted")
                    .build();

        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
            throw new FileStorageException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    public String getFileUrl(String publicId) {
        return cloudinary.url().generate(publicId);
    }

    public String getTransformedImageUrl(String publicId, int width, int height, String crop) {
        return cloudinary.url()
                .transformation(new Transformation()
                        .width(width)
                        .height(height)
                        .crop(crop != null ? crop : "fill"))
                .generate(publicId);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("File type cannot be determined");
        }

        if (!ALLOWED_IMAGE_TYPES.contains(contentType) && !ALLOWED_DOCUMENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException(
                    "File type not allowed. Allowed types: JPEG, PNG, GIF, WEBP, PDF, DOC, DOCX"
            );
        }
    }
}
