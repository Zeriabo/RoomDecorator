package com.room.decorator.model;

public enum DesignStyle {
    FINNISH("Finnish", "Minimalist, natural wood, clean lines, functional design, light colors"),
    SWEDISH("Swedish", "Scandinavian, hygge, neutral tones, cozy textiles, simple elegance"),
    ARABIC("Arabic", "Rich patterns, ornate details, warm colors, geometric designs, luxurious fabrics"),
    RUSSIAN("Russian", "Classical elegance, rich materials, imperial colors, ornate furniture"),
    AMERICAN("American", "Contemporary comfort, mixed materials, bold colors, casual sophistication"),
    MODERN("Modern", "Clean lines, neutral colors, minimal clutter, sleek furniture"),
    TRADITIONAL("Traditional", "Classic furniture, warm colors, patterned fabrics, timeless appeal");
    
    private final String displayName;
    private final String description;
    
    DesignStyle(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }
    
    public static DesignStyle fromString(String style) {
        for (DesignStyle ds : DesignStyle.values()) {
            if (ds.displayName.equalsIgnoreCase(style) || ds.name().equalsIgnoreCase(style)) {
                return ds;
            }
        }
        return MODERN; // Default fallback
    }
}