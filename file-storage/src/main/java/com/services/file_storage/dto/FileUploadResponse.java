package com.services.file_storage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private String publicId;
    private String url;
    private String secureUrl;
    private String format;
    private String resourceType;
    private Long size;
    private Integer width;
    private Integer height;
    private String message;
}
