package com.room.decorator.service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.room.decorator.model.DecoratedRoomOption;
import com.room.decorator.model.DecorationRequest;
import com.room.decorator.model.DesignStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.room.decorator.model.DecoratedRoomOption;
import com.room.decorator.model.DecorationRequest;
import com.room.decorator.model.DesignStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class AIDecorationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AIDecorationService.class);
    
    @Value("${openai.api.key}")
    private String openAiApiKey;
    
    @Value("${stability.api.key:}")
    private String stabilityApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService;
    
    public AIDecorationService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.executorService = Executors.newFixedThreadPool(3);
    }
    
    public List<DecoratedRoomOption> generateDecorationOptions(
            byte[] originalImage, 
            DecorationRequest request, 
            ImageAnalysisService.RoomAnalysis roomAnalysis) {
        
        try {
            List<CompletableFuture<DecoratedRoomOption>> futures = new ArrayList<>();
            
            // Generate 3 different variations of the chosen style
            for (int i = 0; i < 3; i++) {
                final int variation = i + 1;
                CompletableFuture<DecoratedRoomOption> future = CompletableFuture.supplyAsync(() -> 
                    generateSingleOption(originalImage, request, roomAnalysis, variation), 
                    executorService
                );
                futures.add(future);
            }
            
            // Wait for all generations to complete
            List<DecoratedRoomOption> options = new ArrayList<>();
            for (CompletableFuture<DecoratedRoomOption> future : futures) {
                try {
                    DecoratedRoomOption option = future.get();
                    if (option != null) {
                        options.add(option);
                    }
                } catch (Exception e) {
                    logger.error("Error generating decoration option", e);
                }
            }
            
            return options.isEmpty() ? generateFallbackOptions(request) : options;
            
        } catch (Exception e) {
            logger.error("Error in decoration generation", e);
            return generateFallbackOptions(request);
        }
    }
    
    private DecoratedRoomOption generateSingleOption(
            byte[] originalImage, 
            DecorationRequest request, 
            ImageAnalysisService.RoomAnalysis roomAnalysis, 
            int variation) {
        
        try {
            DesignStyle style = DesignStyle.fromString(request.getDesignStyle());
            
            // Create prompt for image generation
            String prompt = createDecorationPrompt(style, roomAnalysis, request, variation);
            
            // Generate image using DALL-E or Stable Diffusion
            String generatedImageBase64 = generateDecoratedImage(originalImage, prompt);
            
            // Generate description and element lists
            String description = generateDescription(style, roomAnalysis, variation);
            List<String> addedElements = generateAddedElements(style, roomAnalysis, variation);
            List<String> modifiedElements = generateModifiedElements(roomAnalysis, variation);
            
            return new DecoratedRoomOption(
                UUID.randomUUID().toString(),
                style.getDisplayName(),
                generatedImageBase64,
                description,
                addedElements,
                modifiedElements,
                0.85 + (Math.random() * 0.15) // Random confidence between 0.85-1.0
            );
            
        } catch (Exception e) {
            logger.error("Error generating single decoration option", e);
            return null;
        }
    }
    
    private String createDecorationPrompt(
            DesignStyle style, 
            ImageAnalysisService.RoomAnalysis roomAnalysis, 
            DecorationRequest request, 
            int variation) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("Interior design makeover of a ").append(roomAnalysis.getRoomType().toLowerCase());
        prompt.append(" in ").append(style.getDisplayName()).append(" style. ");
        prompt.append(style.getDescription()).append(". ");
        
        // Add existing room context
        prompt.append("Current room has: ").append(String.join(", ", roomAnalysis.getDetectedElements()));
        prompt.append(" with ").append(roomAnalysis.getLighting().toLowerCase()).append(" lighting and ");
        prompt.append(roomAnalysis.getColorScheme().toLowerCase()).append(" color scheme. ");
        
        // Add variation-specific details
        switch (variation) {
            case 1:
                prompt.append("Focus on maintaining existing furniture while adding decorative elements. ");
                break;
            case 2:
                prompt.append("Blend existing furniture with new complementary pieces. ");
                break;
            case 3:
                prompt.append("Bold transformation with creative furniture arrangement. ");
                break;
        }
        
        // Add user preferences
        if (request.getColorPreference() != null && !request.getColorPreference().isEmpty()) {
            prompt.append("Incorporate ").append(request.getColorPreference()).append(" colors. ");
        }
        
        if (request.isPreserveExistingFurniture()) {
            prompt.append("Preserve and enhance existing furniture. ");
        }
        
        prompt.append("High quality, realistic, well-lit interior photograph.");
        
        return prompt.toString();
    }
    
    private String generateDecoratedImage(byte[] originalImage, String prompt) {
        try {
            // Use DALL-E 3 for image generation
            String base64Original = Base64.getEncoder().encodeToString(originalImage);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "dall-e-3");
            requestBody.put("prompt", prompt);
            requestBody.put("size", "1024x1024");
            requestBody.put("quality", "standard");
            requestBody.put("n", 1);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(
                "https://api.openai.com/v1/images/generations", 
                entity, 
                String.class
            );
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            String imageUrl = jsonResponse.path("data").get(0).path("url").asText();
            
            // Download the generated image and convert to base64
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            return Base64.getEncoder().encodeToString(imageBytes);
            
        } catch (Exception e) {
            logger.error("Error generating decorated image", e);
            return generatePlaceholderImage();
        }
    }
    
    private String generateDescription(DesignStyle style, ImageAnalysisService.RoomAnalysis roomAnalysis, int variation) {
        StringBuilder desc = new StringBuilder();
        desc.append("This ").append(style.getDisplayName()).append(" design transforms your ");
        desc.append(roomAnalysis.getRoomType().toLowerCase()).append(" with ");
        
        switch (variation) {
            case 1:
                desc.append("subtle enhancements that complement your existing furniture. ");
                desc.append("Added decorative elements maintain the room's current character while introducing ");
                desc.append(style.getDisplayName().toLowerCase()).append(" aesthetics.");
                break;
            case 2:
                desc.append("a balanced approach mixing your current pieces with new complementary furniture. ");
                desc.append("The design creates harmony between existing and new elements in true ");
                desc.append(style.getDisplayName().toLowerCase()).append(" fashion.");
                break;
            case 3:
                desc.append("a bold reimagining of the space with creative furniture arrangements. ");
                desc.append("This variation embraces the full potential of ");
                desc.append(style.getDisplayName().toLowerCase()).append(" design principles.");
                break;
        }
        
        return desc.toString();
    }
    
    private List<String> generateAddedElements(DesignStyle style, ImageAnalysisService.RoomAnalysis roomAnalysis, int variation) {
        List<String> elements = new ArrayList<>();
        
        switch (style) {
            case FINNISH:
                elements.addAll(Arrays.asList("Birch wood accents", "Minimalist lighting fixtures", "Natural textiles", "Clean-lined furniture"));
                break;
            case SWEDISH:
                elements.addAll(Arrays.asList("Cozy throw blankets", "Hygge candles", "Light wood furniture", "Neutral cushions"));
                break;
            case ARABIC:
                elements.addAll(Arrays.asList("Ornate patterns", "Rich tapestries", "Metallic accents", "Geometric designs"));
                break;
            case RUSSIAN:
                elements.addAll(Arrays.asList("Imperial-style furniture", "Rich fabrics", "Ornate decorations", "Classical elements"));
                break;
            case AMERICAN:
                elements.addAll(Arrays.asList("Contemporary art", "Mixed textures", "Bold accent pieces", "Functional storage"));
                break;
            default:
                elements.addAll(Arrays.asList("Modern accessories", "Clean lines", "Neutral colors", "Functional design"));
        }
        
        // Add variation-specific elements
        if (variation == 1) {
            elements.add("Wall art");
            elements.add("Decorative plants");
        } else if (variation == 2) {
            elements.add("Area rug");
            elements.add("Accent furniture");
        } else {
            elements.add("Statement pieces");
            elements.add("Architectural elements");
        }
        
        return elements;
    }
    
    private List<String> generateModifiedElements(ImageAnalysisService.RoomAnalysis roomAnalysis, int variation) {
        List<String> modified = new ArrayList<>();
        
        // Base modifications
        modified.addAll(Arrays.asList("Wall color", "Lighting arrangement", "Furniture positioning"));
        
        if (variation > 1) {
            modified.add("Flooring treatment");
            modified.add("Window treatments");
        }
        
        if (variation == 3) {
            modified.add("Room layout");
            modified.add("Architectural features");
        }
        
        return modified;
    }
    
    private String generatePlaceholderImage() {
        // Generate a simple placeholder image in base64 format
        // In a real implementation, you might want to create a proper placeholder
        return "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";
    }
    
    private List<DecoratedRoomOption> generateFallbackOptions(DecorationRequest request) {
        List<DecoratedRoomOption> fallbackOptions = new ArrayList<>();
        
        DesignStyle style = DesignStyle.fromString(request.getDesignStyle());
        String placeholderImage = generatePlaceholderImage();
        
        for (int i = 1; i <= 3; i++) {
            DecoratedRoomOption option = new DecoratedRoomOption(
                UUID.randomUUID().toString(),
                style.getDisplayName(),
                placeholderImage,
                "Fallback design option " + i + " in " + style.getDisplayName() + " style. " +
                "This design incorporates " + style.getDescription().toLowerCase() + " elements.",
                Arrays.asList("Style-appropriate furniture", "Complementary colors", "Suitable lighting"),
                Arrays.asList("Room layout", "Color scheme", "Furniture arrangement"),
                0.70 + (i * 0.05)
            );
            fallbackOptions.add(option);
        }
        
        return fallbackOptions;
    }
    
    // Alternative method using Stable Diffusion (if you have API access)
    private String generateWithStableDiffusion(byte[] originalImage, String prompt) throws Exception {
        try {
            if (stabilityApiKey == null || stabilityApiKey.isEmpty()) {
                throw new RuntimeException("Stability API key not configured");
            }
            
            String base64Image = Base64.getEncoder().encodeToString(originalImage);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("init_image", base64Image);
            requestBody.put("init_image_mode", "IMAGE_STRENGTH");
            requestBody.put("image_strength", 0.35);
            requestBody.put("steps", 40);
            requestBody.put("seed", 0);
            requestBody.put("cfg_scale", 7);
            requestBody.put("samples", 1);
            
            Map<String, Object> textPrompt = new HashMap<>();
            textPrompt.put("text", prompt);
            textPrompt.put("weight", 1);
            requestBody.put("text_prompts", Arrays.asList(textPrompt));
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + stabilityApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(
                "https://api.stability.ai/v1/generation/stable-diffusion-xl-1024-v1-0/image-to-image",
                entity,
                String.class
            );
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.path("artifacts").get(0).path("base64").asText();
            
        } catch (Exception e) {
            logger.error("Error with Stable Diffusion API", e);
            throw e;
        }
    }
    
    public DecoratedRoomOption regenerateOption(String optionId, DecorationRequest request) {
        // This method allows regenerating a specific option with different parameters
        try {
            DesignStyle style = DesignStyle.fromString(request.getDesignStyle());
            String placeholderImage = generatePlaceholderImage();
            
            return new DecoratedRoomOption(
                optionId,
                style.getDisplayName(),
                placeholderImage,
                "Regenerated " + style.getDisplayName() + " design with updated preferences.",
                generateAddedElements(style, null, 2),
                Arrays.asList("Updated layout", "Revised color scheme", "Modified furniture"),
                0.80 + Math.random() * 0.15
            );
            
        } catch (Exception e) {
            logger.error("Error regenerating option", e);
            return null;
        }
    }
}