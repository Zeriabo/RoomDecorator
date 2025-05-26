package com.room.decorator.model;

public  class DecorationRequestWithImage {
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
