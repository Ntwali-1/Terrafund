package com.services.land_service.mapper;

import com.services.land_service.dto.LandResponse;
import com.services.land_service.dto.LandSummaryResponse;
import com.services.land_service.entity.Land;
import org.springframework.stereotype.Component;

@Component
public class LandMapper {

    public LandResponse toResponse(Land land) {
        if (land == null) {
            return null;
        }

        LandResponse response = new LandResponse();
        response.setId(land.getId());
        response.setOwnerId(land.getOwnerId());
        response.setProvince(land.getProvince());
        response.setDistrict(land.getDistrict());
        response.setSector(land.getSector());
        response.setAreaSqMeters(land.getAreaSqMeters());
        response.setAvailabilityType(land.getAvailabilityType());
        response.setStatus(land.getStatus());
        response.setImageUrls(land.getImageUrls());
        response.setDocumentUrls(land.getDocumentUrls());
        response.setCreatedAt(land.getCreatedAt());
        response.setUpdatedAt(land.getUpdatedAt());

        return response;
    }

    public LandSummaryResponse toSummaryResponse(Land land) {
        if (land == null) {
            return null;
        }

        LandSummaryResponse response = new LandSummaryResponse();
        response.setId(land.getId());
        response.setOwnerId(land.getOwnerId());
        response.setProvince(land.getProvince());
        response.setDistrict(land.getDistrict());
        response.setSector(land.getSector());
        response.setAreaSqMeters(land.getAreaSqMeters());
        response.setAvailabilityType(land.getAvailabilityType());
        response.setStatus(land.getStatus());
        response.setCreatedAt(land.getCreatedAt());

        if (land.getImageUrls() != null && !land.getImageUrls().isEmpty()) {
            response.setThumbnailUrl(land.getImageUrls().get(0));
        }

        return response;
    }
}