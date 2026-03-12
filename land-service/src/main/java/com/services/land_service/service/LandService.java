package com.services.land_service.service;

import com.services.land_service.dto.*;
import com.services.land_service.entity.Land.LandStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LandService {

    LandResponse createLand(CreateLandRequest request,Long currentUserId);

    LandResponse getLandById(Long id);

    Page<LandSummaryResponse> getAllLands(Pageable pageable);

    LandResponse updateLand(Long id, UpdateLandRequest request, Long currentUserId);

    LandResponse updateLandStatus(Long id, LandStatus status, Long currentUserId);

    void deleteLand(Long id, Long currentUserId);

    Page<LandSummaryResponse> searchLandsByLocation(String province, String district, String sector, Pageable pageable);

    Page<LandSummaryResponse> getLandsByStatus(LandStatus status, Pageable pageable);

    Page<LandSummaryResponse> getLandsByAreaRange(Double minArea, Double maxArea, Pageable pageable);

    LandResponse addImages(Long id, List<String> imageUrls, Long currentUserId);

    LandResponse addDocuments(Long id, List<String> documentUrls, Long currentUserId);

    LandResponse removeImage(Long id, String imageUrl, Long currentUserId);

    LandResponse removeDocument(Long id, String documentUrl,Long currentUserId);

    Page<LandSummaryResponse> getLandsByOwner(Long ownerId, Pageable pageable);
}