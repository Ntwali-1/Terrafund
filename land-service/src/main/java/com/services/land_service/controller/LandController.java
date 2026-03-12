package com.services.land_service.controller;

import com.services.land_service.dto.*;
import com.services.land_service.entity.Land.LandStatus;
import com.services.land_service.security.SecurityUtils;
import com.services.land_service.service.LandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/lands")
@RequiredArgsConstructor
public class LandController {

    private final LandService landService;

    /**
     * Create a new land
     * POST /api/lands
     */
    @PostMapping
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> createLand(
            @Valid @RequestBody CreateLandRequest request
    ) {
        log.info("➡️  [CREATE LAND] Request received");

        Long currentUserId = SecurityUtils.getCurrentUserId();
        log.info("👤 Authenticated user ID: {}", currentUserId);

        log.info("📝 Land request payload: {}", request);

        LandResponse response = landService.createLand(request, currentUserId);

        log.info("✅ Land created successfully with ID: {}", response.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<LandResponse> getLandById(@PathVariable Long id) {
        LandResponse response = landService.getLandById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<LandSummaryResponse>> getAllLands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("ASC")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LandSummaryResponse> response = landService.getAllLands(pageable);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> updateLand(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLandRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        LandResponse response = landService.updateLand(id, request, currentUserId);
        return ResponseEntity.ok(response);
    }
    
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> updateLandStatus(
            @PathVariable Long id,
            @RequestParam LandStatus status) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        LandResponse response = landService.updateLandStatus(id, status, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<Void> deleteLand(@PathVariable Long id) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        landService.deleteLand(id,currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search/location")
    public ResponseEntity<Page<LandSummaryResponse>> searchLandsByLocation(
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String sector,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LandSummaryResponse> response = landService.searchLandsByLocation(
                province, district, sector, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LandSummaryResponse>> getLandsByStatus(
            @PathVariable LandStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LandSummaryResponse> response = landService.getLandsByStatus(status, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/area")
    public ResponseEntity<Page<LandSummaryResponse>> getLandsByAreaRange(
            @RequestParam Double minArea,
            @RequestParam Double maxArea,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<LandSummaryResponse> response = landService.getLandsByAreaRange(
                minArea, maxArea, pageable);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<Page<LandSummaryResponse>> getMyLands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long currentUserId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<LandSummaryResponse> response = landService.getLandsByOwner(currentUserId, pageable);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/images")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> addImages(
            @PathVariable Long id,
            @Valid @RequestBody UploadImagesRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        LandResponse response = landService.addImages(id, request.getImageUrls(), currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> addDocuments(
            @PathVariable Long id,
            @Valid @RequestBody UploadDocumentsRequest request) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        LandResponse response = landService.addDocuments(id, request.getDocumentUrls(), currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/images")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> removeImage(
            @PathVariable Long id,
            @RequestParam String imageUrl) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        LandResponse response = landService.removeImage(id, imageUrl, currentUserId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/documents")
    @PreAuthorize("hasRole('LAND_OWNER')")
    public ResponseEntity<LandResponse> removeDocument(
            @PathVariable Long id,
            @RequestParam String documentUrl) {

        Long currentUserId = SecurityUtils.getCurrentUserId();

        LandResponse response = landService.removeDocument(id, documentUrl, currentUserId);
        return ResponseEntity.ok(response);
    }
}