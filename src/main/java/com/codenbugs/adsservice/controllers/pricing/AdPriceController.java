package com.codenbugs.adsservice.controllers.pricing;

import com.codenbugs.adsservice.dtos.pricing.AdPriceRequest;
import com.codenbugs.adsservice.dtos.pricing.AdPriceResponse;
import com.codenbugs.adsservice.dtos.pricing.AdPriceUpdateRequest;
import com.codenbugs.adsservice.exceptions.BusinessRuleException;
import com.codenbugs.adsservice.exceptions.NotFoundException;
import com.codenbugs.adsservice.services.pricing.AdPriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/ads/prices")
@Tag(name = "Ad Price Management", description = "Endpoints for managing ad pricing")
public class AdPriceController {

    private final AdPriceService adPriceService;

    public AdPriceController(AdPriceService adPriceService) {
        this.adPriceService = adPriceService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Create a new ad price", description = "Creates a new ad price for a specific ad type and period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ad price created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AdPriceResponse> createAdPrice(@Valid @RequestBody AdPriceRequest request, @AuthenticationPrincipal Jwt jwt) throws BusinessRuleException {
        UUID adminId = UUID.fromString(jwt.getSubject());
        AdPriceResponse response = adPriceService.createAdPrice(request, adminId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping()
    @PreAuthorize("hasRole('ADVERTISER') or hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Get all ad prices", description = "Retrieves a list of all ad prices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ad prices retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<List<AdPriceResponse>> getAllAdPrices() throws BusinessRuleException {
        List<AdPriceResponse> response = adPriceService.getAllAdPrices();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{idPrice}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Update an ad price", description = "Updates the price of an existing ad price by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ad price updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Ad price not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<AdPriceResponse> updateAdPrice(@PathVariable("idPrice") UUID idPrice, @Valid @RequestBody AdPriceUpdateRequest request, @AuthenticationPrincipal Jwt jwt) throws BusinessRuleException, NotFoundException {
        UUID adminId = UUID.fromString(jwt.getSubject());
        AdPriceResponse response = adPriceService.updateAdPrice(idPrice, request, adminId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{idPrice}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Delete an ad price", description = "Deletes an ad price by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ad price deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Ad price not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public ResponseEntity<Void> deleteAdPrice(@PathVariable("idPrice") UUID idPrice) throws BusinessRuleException, NotFoundException {
        adPriceService.deleteAdPrice(idPrice);
        return ResponseEntity.noContent().build();
    }


}
