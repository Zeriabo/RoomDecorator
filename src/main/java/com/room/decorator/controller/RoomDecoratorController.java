// RoomDecoratorController.java
package com.room.decorator.controller;

import com.room.decorator.model.DecoratedRoomOption;
import com.room.decorator.model.DecorationRequest;
import com.room.decorator.model.DecorationResponse;
import com.room.decorator.service.AIDecorationService;
import com.room.decorator.service.ImageAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/room-decorator")
@CrossOrigin(origins = "*") // Configure this properly for production
public class RoomDecoratorController {
    
    private static final Logger logger = LoggerFactory.getLogger(RoomDecoratorController.class);
    
    @Autowired
    private ImageAnalysisService imageAnalysisService;
    
    @Autowired
    private AIDecorationService aiDecorationService;
    
    @PostMapping(value = "/decorate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DecorationResponse> decorateRoom(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam("designStyle") String designStyle,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "colorPreference", required = false) String colorPreference,
            @RequestParam(value = "budgetRange", required = false) String budgetRange,
            @RequestParam(value = "preserveExistingFurniture", defaultValue = "true") boolean preserveExistingFurniture) {
        
        try {
            // Validate input
            if (imageFile.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new DecorationResponse(null, "", false, "No image provided"));
            }
            
            if (!isValidImageType(imageFile.getContentType())) {
                return ResponseEntity.badRequest()
                    .body(new DecorationResponse(null, "", false, "Invalid image format. Please use JPG, PNG, or WEBP"));
            }
            
            // Create decoration request object
            DecorationRequest request = new DecorationRequest(
                designStyle, roomType, colorPreference, budgetRange, preserveExistingFurniture
            );
            
            // Get image bytes
            byte[] imageBytes = imageFile.getBytes();
            
            // Analyze the room
            logger.info("Analyzing room image for style: {}", designStyle);
            ImageAnalysisService.RoomAnalysis roomAnalysis = imageAnalysisService.analyzeRoom(imageBytes);
            
            // Generate decoration options
            logger.info("Generating decoration options...");
            List<DecoratedRoomOption> options = aiDecorationService.generateDecorationOptions(
                imageBytes, request, roomAnalysis
            );
            
            // Create response
            DecorationResponse response = new DecorationResponse(
                options,
                roomAnalysis.getAiDescription(),
                true,
                "Successfully generated " + options.size() + " decoration options"
            );
            
            logger.info("Successfully generated {} decoration options", options.size());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing decoration request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DecorationResponse(null, "", false, "Internal server error: " + e.getMessage()));
        }
    }
    
    @PostMapping(value = "/decorate-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DecorationResponse> decorateRoomJson(
            @Valid @RequestBody DecorationRequestWithImage request) {
        
        try {
            // Decode base64 image
            byte[] imageBytes = java.util.Base64.getDecoder().decode(request.getImageBase64());
            
            // Create decoration request
            DecorationRequest decorationRequest = new DecorationRequest(
                request.getDesignStyle(),
                request.getRoomType(),
                request.getColorPreference(),
                request.getBudgetRange(),
                request.isPreserveExistingFurniture()
            );
            
            // Analyze the room
            ImageAnalysisService.RoomAnalysis roomAnalysis = imageAnalysisService.analyzeRoom(imageBytes);
            
            // Generate decoration options
            List<DecoratedRoomOption> options = aiDecorationService.generateDecorationOptions(
                imageBytes, decorationRequest, roomAnalysis
            );
            
            // Create response
            DecorationResponse response = new DecorationResponse(
                options,
                roomAnalysis.getAiDescription(),
                true,
                "Successfully generated " + options.size() + " decoration options"
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing JSON decoration request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DecorationResponse(null, "", false, "Error processing request: " + e.getMessage()));
        }
    }
    
    @GetMapping("/styles")
    public ResponseEntity<List<String>> getAvailableStyles() {
        List<String> styles = Arrays.asList(
            "Finnish", "Swedish", "Arabic", "Russian", "American", "Modern", "Traditional"
        );
        return ResponseEntity.ok(styles);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Room Decorator API is running");
    }
    
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
            contentType.equals("image/jpeg") ||
            contentType.equals("image/jpg") ||
            contentType.equals("image/png") ||
            contentType.equals("image/webp")
        );
    }
    
    // Inner class for JSON requests
    public static class DecorationRequestWithImage {
        private String imageBase64;
        private String designStyle;
        private String roomType;
        private String colorPreference;
        private String budgetRange;
        private boolean preserveExistingFurniture = true;
        
        // Constructors
        public DecorationRequestWithImage() {}
        
        // Getters and Setters
        public String getImageBase64() { return imageBase64; }
        public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
        
        public String getDesignStyle() { return designStyle; }
        public void setDesignStyle(String designStyle) { this.designStyle = designStyle; }
        
        public String getRoomType() { return roomType; }
        public void setRoomType(String roomType) { this.roomType = roomType; }
        
        public String getColorPreference() { return colorPreference; }
        public void setColorPreference(String colorPreference) { this.colorPreference = colorPreference; }
        
        public String getBudgetRange() { return budgetRange; }
        public void setBudgetRange(String budgetRange) { this.budgetRange = budgetRange; }
        
        public boolean isPreserveExistingFurniture() { return preserveExistingFurniture; }
        public void setPreserveExistingFurniture(boolean preserveExistingFurniture) { 
            this.preserveExistingFurniture = preserveExistingFurniture; 
        }
    }
}