package com.services.land_service.service.impl;

import com.services.land_service.dto.*;
import com.services.land_service.entity.Land;
import com.services.land_service.entity.Land.LandStatus;
import com.services.land_service.exception.LandNotFoundException;
import com.services.land_service.mapper.LandMapper;
import com.services.land_service.repository.LandRepository;
import com.services.land_service.service.LandService;
import jakarta.ws.rs.ForbiddenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LandServiceImpl implements LandService {

    private final LandRepository landRepository;
    private final LandMapper landMapper;

    @Override
    @Transactional
    public LandResponse createLand(CreateLandRequest request,Long currentUserId) {
        log.info("Creating new land in {}, {}, {}", request.getProvince(), request.getDistrict(), request.getSector());

        Land land = new Land();
        land.setOwnerId(currentUserId);
        land.setProvince(request.getProvince());
        land.setDistrict(request.getDistrict());
        land.setSector(request.getSector());
        land.setAreaSqMeters(request.getAreaSqMeters());
        land.setAvailabilityType(request.getAvailabilityType());
        land.setStatus(LandStatus.AVAILABLE);
        land.setImageUrls(request.getImageUrls() != null ? request.getImageUrls() : new ArrayList<>());
        land.setDocumentUrls(request.getDocumentUrls() != null ? request.getDocumentUrls() : new ArrayList<>());

        Land savedLand = landRepository.save(land);
        log.info("Land created successfully with ID: {}", savedLand.getId());

        return landMapper.toResponse(savedLand);
    }

    @Override
    @Transactional(readOnly = true)
    public LandResponse getLandById(Long id) {
        log.info("Fetching land with ID: {}", id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        return landMapper.toResponse(land);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LandSummaryResponse> getAllLands(Pageable pageable) {
        log.info("Fetching all lands with pagination");

        return landRepository.findAll(pageable)
                .map(landMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public LandResponse updateLand(Long id, UpdateLandRequest request, Long currentUserId) {
        log.info("Updating land with ID: {}", id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to update this land. Only the owner can update it.");
        }

        // Update only provided fields
        if (request.getProvince() != null) {
            land.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            land.setDistrict(request.getDistrict());
        }
        if (request.getSector() != null) {
            land.setSector(request.getSector());
        }
        if (request.getAreaSqMeters() != null) {
            land.setAreaSqMeters(request.getAreaSqMeters());
        }
        if (request.getAvailabilityType() != null) {
            land.setAvailabilityType(request.getAvailabilityType());
        }
        if (request.getImageUrls() != null) {
            land.setImageUrls(request.getImageUrls());
        }
        if (request.getDocumentUrls() != null) {
            land.setDocumentUrls(request.getDocumentUrls());
        }

        Land updatedLand = landRepository.save(land);
        log.info("Land updated successfully: {}", id);

        return landMapper.toResponse(updatedLand);
    }

    @Override
    @Transactional
    public LandResponse updateLandStatus(Long id, LandStatus status, Long currentUserId) {
        log.info("Updating land status for ID: {} to {}", id, status);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to update this land status. Only the owner can update it.");
        }

        land.setStatus(status);
        Land updatedLand = landRepository.save(land);

        log.info("Land status updated successfully");
        return landMapper.toResponse(updatedLand);
    }

    @Override
    @Transactional
    public void deleteLand(Long id, Long currentUserId) {
        log.info("Deleting land with ID: {}", id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        // Check ownership
        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to delete this land. Only the owner can delete it.");
        }

        landRepository.deleteById(id);
        log.info("Land deleted successfully: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LandSummaryResponse> searchLandsByLocation(String province, String district, String sector, Pageable pageable) {
        log.info("Searching lands by location - Province: {}, District: {}, Sector: {}", province, district, sector);

        if (sector != null && !sector.isBlank()) {
            return landRepository.findBySectorContainingIgnoreCase(sector, pageable)
                    .map(landMapper::toSummaryResponse);
        } else if (district != null && !district.isBlank()) {
            return landRepository.findByDistrictContainingIgnoreCase(district, pageable)
                    .map(landMapper::toSummaryResponse);
        } else if (province != null && !province.isBlank()) {
            return landRepository.findByProvinceContainingIgnoreCase(province, pageable)
                    .map(landMapper::toSummaryResponse);
        }

        return Page.empty();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LandSummaryResponse> getLandsByStatus(LandStatus status, Pageable pageable) {
        log.info("Fetching lands with status: {}", status);

        return landRepository.findByStatus(status, pageable)
                .map(landMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LandSummaryResponse> getLandsByAreaRange(Double minArea, Double maxArea, Pageable pageable) {
        log.info("Fetching lands with area between {} and {} sq meters", minArea, maxArea);

        return landRepository.findByAreaSqMetersBetween(minArea, maxArea, pageable)
                .map(landMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public LandResponse addImages(Long id, List<String> imageUrls, Long currentUserId) {
        log.info("Adding {} images to land ID: {}", imageUrls.size(), id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        // Check ownership
        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to upload images this land. Only the owner can do it.");
        }

        land.getImageUrls().addAll(imageUrls);
        Land updatedLand = landRepository.save(land);

        log.info("Images added successfully");
        return landMapper.toResponse(updatedLand);
    }

    @Override
    @Transactional
    public LandResponse addDocuments(Long id, List<String> documentUrls, Long currentUserId) {
        log.info("Adding {} documents to land ID: {}", documentUrls.size(), id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        // Check ownership
        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to upload documents to this land. Only the owner can do it.");
        }

        land.getDocumentUrls().addAll(documentUrls);
        Land updatedLand = landRepository.save(land);

        log.info("Documents added successfully");
        return landMapper.toResponse(updatedLand);
    }

    @Override
    @Transactional
    public LandResponse removeImage(Long id, String imageUrl, Long currentUserId) {
        log.info("Removing image from land ID: {}", id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        // Check ownership
        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to upload documents to this land. Only the owner can do it.");
        }

        land.getImageUrls().remove(imageUrl);
        Land updatedLand = landRepository.save(land);

        log.info("Image removed successfully");
        return landMapper.toResponse(updatedLand);
    }

    @Override
    @Transactional
    public LandResponse removeDocument(Long id, String documentUrl, Long currentUserId) {
        log.info("Removing document from land ID: {}", id);

        Land land = landRepository.findById(id)
                .orElseThrow(() -> new LandNotFoundException("Land not found with ID: " + id));

        // Check ownership
        if (!land.getOwnerId().equals(currentUserId)) {
            throw new ForbiddenException("You don't have permission to upload documents to this land. Only the owner can do it.");
        }

        land.getDocumentUrls().remove(documentUrl);
        Land updatedLand = landRepository.save(land);

        log.info("Document removed successfully");
        return landMapper.toResponse(updatedLand);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LandSummaryResponse> getLandsByOwner(Long ownerId, Pageable pageable) {
        log.info("Fetching lands for owner ID: {}", ownerId);
        return landRepository.findByOwnerId(ownerId, pageable)
                .map(landMapper::toSummaryResponse);
    }
}