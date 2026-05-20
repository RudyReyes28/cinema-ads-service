package com.codenbugs.adsservice.controllers.ads;

import com.codenbugs.adsservice.dtos.ads.AdRequest;
import com.codenbugs.adsservice.dtos.ads.AdResponse;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.FileStorageException;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import com.codenbugs.adsservice.services.ads.AdService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ads")
@Tag(name = "Ad Management", description = "Endpoints for managing advertisements")
public class AdController {

    private final AdService adService;

    public AdController(AdService adService) {
        this.adService = adService;
    }

    @GetMapping("/advertiser")
    @PreAuthorize("hasRole('ADVERTISER')")
    @Operation(summary = "Get ads by advertiser", description = "Retrieves a list of ads created by the authenticated advertiser")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ads retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<AdResponse>> getAdsByAdvertiser(@AuthenticationPrincipal Jwt jwt) throws NotFoundException {
        UUID advertiserId = UUID.fromString(jwt.getSubject());
        List<AdResponse> response = adService.getAdsByAdvertiser(advertiserId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active ads", description = "Retrieves a list of all active ads")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Active ads retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<AdResponse>> getActiveAds() throws NotFoundException {
        List<AdResponse> response = adService.getActiveAds();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/desactivate/{adId}")
    @PreAuthorize("hasRole('ADVERTISER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Desactivate an ad", description = "Desactivates an ad by its ID. Advertisers can only desactivate their own ads, while system admins can desactivate any ad")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ad desactivated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Ad not found")
    })
    public ResponseEntity<AdResponse> desactivateAd(@PathVariable UUID adId, @AuthenticationPrincipal Jwt jwt) throws NotFoundException, BusinessRuleException {
        UUID advertiserId = UUID.fromString(jwt.getSubject());
        AdResponse response = adService.desactivateAd(adId, advertiserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADVERTISER')")
    @Operation(summary = "Create a new ad", description = "Creates a new ad for the authenticated advertiser. The ad will be active upon creation and the advertiser will be charged according to the ad price")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ad created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AdResponse> createAd(
            @Valid @RequestPart("request") AdRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart(value = "videoFile", required = false) MultipartFile videoFile,
            @AuthenticationPrincipal Jwt jwt
    ) throws BusinessRuleException, NotFoundException, FileStorageException {
        UUID advertiserId = UUID.fromString(jwt.getSubject());
        AdResponse response = adService.createAd(request, image, videoFile, advertiserId, jwt.getTokenValue());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
