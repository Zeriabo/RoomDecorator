// DecoratedRoomOption.java
package com.room.decorator.model;

import java.util.List;

public class DecoratedRoomOption {
    private String id;
    private String designStyle;
    private String imageBase64;
    private String description;
    private List<String> addedElements;
    private List<String> modifiedElements;
    private double confidenceScore;
    
    // Constructors
    public DecoratedRoomOption() {}
    
    public DecoratedRoomOption(String id, String designStyle, String imageBase64, 
                             String description, List<String> addedElements, 
                             List<String> modifiedElements, double confidenceScore) {
        this.id = id;
        this.designStyle = designStyle;
        this.imageBase64 = imageBase64;
        this.description = description;
        this.addedElements = addedElements;
        this.modifiedElements = modifiedElements;
        this.confidenceScore = confidenceScore;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getDesignStyle() { return designStyle; }
    public void setDesignStyle(String designStyle) { this.designStyle = designStyle; }
    
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public List<String> getAddedElements() { return addedElements; }
    public void setAddedElements(List<String> addedElements) { this.addedElements = addedElements; }
    
    public List<String> getModifiedElements() { return modifiedElements; }
    public void setModifiedElements(List<String> modifiedElements) { 
        this.modifiedElements = modifiedElements; 
    }
    
    public double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(double confidenceScore) { this.confidenceScore = confidenceScore; }
}
