package com.room.decorator.model;

import com.room.decorator.service.ImageAnalysisService;

public  class RoomAnalysisResponse {
    private ImageAnalysisService.RoomAnalysis analysis;
    private boolean success;
    private String message;
    
    public RoomAnalysisResponse(ImageAnalysisService.RoomAnalysis analysis, boolean success, String message) {
        this.analysis = analysis;
        this.success = success;
        this.message = message;
    }
    
    public ImageAnalysisService.RoomAnalysis getAnalysis() { return analysis; }
    public void setAnalysis(ImageAnalysisService.RoomAnalysis analysis) { this.analysis = analysis; }
    
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
