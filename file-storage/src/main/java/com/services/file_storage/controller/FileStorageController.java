package com.services.file_storage.controller;

import com.services.file_storage.dto.FileDeleteResponse;
import com.services.file_storage.dto.FileUploadResponse;
import com.services.file_storage.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "File Storage", description = "APIs for file upload, download, and management using Cloudinary")
public class FileStorageController {

    private final FileStorageService fileStorageService;

    @Operation(
            summary = "Upload a single file",
            description = "Upload a single file (image or document) to Cloudinary. Supports JPEG, PNG, GIF, WEBP, PDF, DOC, DOCX formats. Max size: 10MB"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "File uploaded successfully",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file or file type not allowed"),
            @ApiResponse(responseCode = "413", description = "File size exceeds 10MB limit"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(
            @Parameter(description = "File to upload", required = true)
            @RequestParam("file") MultipartFile file,
            @Parameter(description = "Folder name in Cloudinary (optional, defaults to 'uploads')")
            @RequestParam(value = "folder", required = false) String folder) {
        FileUploadResponse response = fileStorageService.uploadFile(file, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Upload multiple files",
            description = "Upload multiple files at once to Cloudinary. Each file is validated independently."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Files uploaded (check individual responses for status)",
                    content = @Content(schema = @Schema(implementation = FileUploadResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/upload-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResponse>> uploadMultipleFiles(
            @Parameter(description = "List of files to upload", required = true)
            @RequestParam("files") List<MultipartFile> files,
            @Parameter(description = "Folder name in Cloudinary (optional)")
            @RequestParam(value = "folder", required = false) String folder) {
        List<FileUploadResponse> responses = fileStorageService.uploadMultipleFiles(files, folder);
        return ResponseEntity.status(HttpStatus.CREATED).body(responses);
    }

    @Operation(
            summary = "Delete a file",
            description = "Delete a file from Cloudinary using its public ID. Replace '/' with '_' in the public ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "File deleted successfully",
                    content = @Content(schema = @Schema(implementation = FileDeleteResponse.class))
            ),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/delete/{publicId}")
    public ResponseEntity<FileDeleteResponse> deleteFile(
            @Parameter(description = "Public ID of the file (replace '/' with '_')", example = "uploads_image_abc123")
            @PathVariable String publicId) {
        // Replace underscores with forward slashes in path variable
        String decodedPublicId = publicId.replace("_", "/");
        FileDeleteResponse response = fileStorageService.deleteFile(decodedPublicId);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get file URL",
            description = "Get the Cloudinary URL for a file using its public ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/url/{publicId}")
    public ResponseEntity<String> getFileUrl(
            @Parameter(description = "Public ID of the file (replace '/' with '_')", example = "uploads_image_abc123")
            @PathVariable String publicId) {
        String decodedPublicId = publicId.replace("_", "/");
        String url = fileStorageService.getFileUrl(decodedPublicId);
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Get transformed image URL",
            description = "Get a transformed version of an image with custom dimensions and crop settings"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transformed URL generated successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @GetMapping("/transform/{publicId}")
    public ResponseEntity<String> getTransformedImageUrl(
            @Parameter(description = "Public ID of the image (replace '/' with '_')", example = "uploads_image_abc123")
            @PathVariable String publicId,
            @Parameter(description = "Width in pixels", example = "300")
            @RequestParam(defaultValue = "300") int width,
            @Parameter(description = "Height in pixels", example = "300")
            @RequestParam(defaultValue = "300") int height,
            @Parameter(description = "Crop mode: fill, fit, scale, crop, thumb", example = "fill")
            @RequestParam(defaultValue = "fill") String crop) {
        String decodedPublicId = publicId.replace("_", "/");
        String url = fileStorageService.getTransformedImageUrl(decodedPublicId, width, height, crop);
        return ResponseEntity.ok(url);
    }

    @Operation(
            summary = "Health check",
            description = "Check if the File Storage Service is running"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is healthy")
    })
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("File Storage Service is running");
    }
}