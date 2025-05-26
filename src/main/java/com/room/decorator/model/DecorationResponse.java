package com.room.decorator.model;

import java.util.List;

public class DecorationResponse {
    private List<DecoratedRoomOption> options;
    private String originalImageAnalysis;
    private boolean success;
    private String message;
    
    // Constructors
    public DecorationResponse() {}
    
    public DecorationResponse(List<DecoratedRoomOption> options, String originalImageAnalysis, 
                            boolean success, String message) {
        this.options = options;
        this.originalImageAnalysis = originalImageAnalysis;
        this.success = success;
        this.message = message;
    }
    
    // Getters and Setters
    public List<DecoratedRoomOption> getOptions() { return options; }
    public void setOptions(List<DecoratedRoomOption> options) { this.options = options; }
    
    public String getOriginalImageAnalysis() { return originalImageAnalysis; }
    public void setOriginalImageAnalysis(String originalImageAnalysis) { 
        this.originalImageAnalysis = originalImageAnalysis; 
    }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
