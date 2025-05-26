// DecorationRequest.java
package com.room.decorator.model;

import jakarta.validation.constraints.NotNull;

public class DecorationRequest {
    @NotNull
    private String designStyle;
    
    private String roomType;
    private String colorPreference;
    private String budgetRange;
    private boolean preserveExistingFurniture;
    
    // Constructors
    public DecorationRequest() {}
    
    public DecorationRequest(String designStyle, String roomType, String colorPreference, 
                           String budgetRange, boolean preserveExistingFurniture) {
        this.designStyle = designStyle;
        this.roomType = roomType;
        this.colorPreference = colorPreference;
        this.budgetRange = budgetRange;
        this.preserveExistingFurniture = preserveExistingFurniture;
    }
    
    // Getters and Setters
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


