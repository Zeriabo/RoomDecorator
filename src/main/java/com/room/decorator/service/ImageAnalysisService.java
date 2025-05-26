// ImageAnalysisService.java
package com.room.decorator.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import static org.opencv.imgcodecs.Imgcodecs.imdecode;


@Service
public class ImageAnalysisService {
    
    private static final Logger logger = LoggerFactory.getLogger(ImageAnalysisService.class);
    
    @Value("${openai.api.key}")
    private String openAiApiKey;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
  
    
    public ImageAnalysisService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    public RoomAnalysis analyzeRoom(byte[] imageData) {
        try {
            // Basic computer vision analysis
            Mat image = imdecode(new MatOfByte(imageData), Imgcodecs.IMREAD_COLOR);
            
            // Detect furniture and room elements using OpenCV
            List<String> detectedElements = detectRoomElements(image);
            
            // Use OpenAI Vision API for detailed analysis
            String aiAnalysis = analyzeWithOpenAI(imageData);
            
            return new RoomAnalysis(detectedElements, aiAnalysis, 
                                  determineRoomType(detectedElements, aiAnalysis),
                                  analyzeLighting(image),
                                  analyzeColorScheme(image));
            
        } catch (Exception e) {
            logger.error("Error analyzing room image", e);
            return new RoomAnalysis(Arrays.asList("Unable to analyze"), 
                                  "Analysis failed", "Unknown", "Natural", "Neutral");
        }
    }
    
    private List<String> detectRoomElements(Mat image) {
        List<String> elements = new ArrayList<>();
        
        try {
            // Convert to grayscale for edge detection
            Mat gray = new Mat();
            Imgproc.cvtColor(image, gray, Imgproc.COLOR_BGR2GRAY);
            
            // Detect edges
            Mat edges = new Mat();
            Imgproc.Canny(gray, edges, 50, 150);
            
            // Find contours (simplified furniture detection)
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(edges, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            
            // Analyze contours to identify potential furniture
            for (MatOfPoint contour : contours) {
                Rect boundingRect = Imgproc.boundingRect(contour);
                double area = Imgproc.contourArea(contour);
                
                // Basic heuristics for furniture detection
                if (area > 5000) { // Large objects
                    double aspectRatio = (double) boundingRect.width / boundingRect.height;
                    
                    if (aspectRatio > 1.5 && aspectRatio < 3.0) {
                        elements.add("Table/Desk");
                    } else if (aspectRatio > 0.7 && aspectRatio < 1.3) {
                        elements.add("Chair/Square Furniture");
                    } else if (aspectRatio > 3.0) {
                        elements.add("Sofa/Long Furniture");
                    }
                }
            }
            
            // Add some common room elements as defaults
            if (elements.isEmpty()) {
                elements.addAll(Arrays.asList("Walls", "Floor", "General Furniture"));
            }
            
        } catch (Exception e) {
            logger.error("Error in OpenCV analysis", e);
            elements.add("Basic Room Structure");
        }
        
        return elements;
    }
    
    private String analyzeWithOpenAI(byte[] imageData) {
        try {
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "gpt-4-vision-preview");
            requestBody.put("max_tokens", 500);
            
            List<Map<String, Object>> messages = new ArrayList<>();
            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            
            List<Map<String, Object>> content = new ArrayList<>();
            
            // Text part
            Map<String, Object> textContent = new HashMap<>();
            textContent.put("type", "text");
            textContent.put("text", "Analyze this room image. Identify furniture, room type, color scheme, lighting, and style. Provide a detailed description for interior decoration purposes.");
            content.add(textContent);
            
            // Image part
            Map<String, Object> imageContent = new HashMap<>();
            imageContent.put("type", "image_url");
            Map<String, Object> imageUrl = new HashMap<>();
            imageUrl.put("url", "data:image/jpeg;base64," + base64Image);
            imageContent.put("image_url", imageUrl);
            content.add(imageContent);
            
            message.put("content", content);
            messages.add(message);
            requestBody.put("messages", messages);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(openAiApiKey);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(
                "https://api.openai.com/v1/chat/completions", 
                entity, 
                String.class
            );
            
            JsonNode jsonResponse = objectMapper.readTree(response);
            return jsonResponse.path("choices").get(0).path("message").path("content").asText();
            
        } catch (Exception e) {
            logger.error("Error calling OpenAI Vision API", e);
            return "AI analysis unavailable. Basic room detected with standard furniture layout.";
        }
    }
    
    private String determineRoomType(List<String> elements, String aiAnalysis) {
        String analysisLower = aiAnalysis.toLowerCase();
        
        if (analysisLower.contains("kitchen") || analysisLower.contains("stove") || analysisLower.contains("refrigerator")) {
            return "Kitchen";
        } else if (analysisLower.contains("bedroom") || analysisLower.contains("bed")) {
            return "Bedroom";
        } else if (analysisLower.contains("bathroom") || analysisLower.contains("toilet") || analysisLower.contains("shower")) {
            return "Bathroom";
        } else if (analysisLower.contains("dining") || analysisLower.contains("dining table")) {
            return "Dining Room";
        } else if (analysisLower.contains("living") || analysisLower.contains("sofa") || analysisLower.contains("couch")) {
            return "Living Room";
        } else {
            return "General Room";
        }
    }
    
    private String analyzeLighting(Mat image) {
        Scalar meanBrightness = Core.mean(image);
        double brightness = (meanBrightness.val[0] + meanBrightness.val[1] + meanBrightness.val[2]) / 3;
        
        if (brightness > 180) {
            return "Bright";
        } else if (brightness > 120) {
            return "Natural";
        } else {
            return "Dim";
        }
    }
    
    private String analyzeColorScheme(Mat image) {
        // Simplified color analysis
        Scalar meanColor = Core.mean(image);
        double blue = meanColor.val[0];
        double green = meanColor.val[1];
        double red = meanColor.val[2];
        
        if (red > green && red > blue) {
            return "Warm";
        } else if (blue > red && blue > green) {
            return "Cool";
        } else {
            return "Neutral";
        }
    }
    
    // Inner class for room analysis results
    public static class RoomAnalysis {
        private final List<String> detectedElements;
        private final String aiDescription;
        private final String roomType;
        private final String lighting;
        private final String colorScheme;
        
        public RoomAnalysis(List<String> detectedElements, String aiDescription, 
                          String roomType, String lighting, String colorScheme) {
            this.detectedElements = detectedElements;
            this.aiDescription = aiDescription;
            this.roomType = roomType;
            this.lighting = lighting;
            this.colorScheme = colorScheme;
        }
        
        // Getters
        public List<String> getDetectedElements() { return detectedElements; }
        public String getAiDescription() { return aiDescription; }
        public String getRoomType() { return roomType; }
        public String getLighting() { return lighting; }
        public String getColorScheme() { return colorScheme; }
    }
}