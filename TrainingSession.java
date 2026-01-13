package com.shooterpro.app.models;

import java.util.ArrayList;
import java.util.List;

public class TrainingSession {
    private long id;
    private long shooterId;
    private String date;
    private String type; // "training" или "test"
    private double totalScore;
    private double averageScore;
    private double bestShot;
    private double worstShot;
    private int breaksCount;
    private int shotsCount;
    private long timeSpent; // в секундах
    private String notes;
    private List<Shot> shots = new ArrayList<>();
    
    public TrainingSession() {}
    
    public TrainingSession(long shooterId, String date, String type) {
        this.shooterId = shooterId;
        this.date = date;
        this.type = type;
    }
    
    // Геттеры и сеттеры
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getShooterId() { return shooterId; }
    public void setShooterId(long shooterId) { this.shooterId = shooterId; }
    
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public double getTotalScore() { return totalScore; }
    public void setTotalScore(double totalScore) { this.totalScore = totalScore; }
    
    public double getAverageScore() { return averageScore; }
    public void setAverageScore(double averageScore) { this.averageScore = averageScore; }
    
    public double getBestShot() { return bestShot; }
    public void setBestShot(double bestShot) { this.bestShot = bestShot; }
    
    public double getWorstShot() { return worstShot; }
    public void setWorstShot(double worstShot) { this.worstShot = worstShot; }
    
    public int getBreaksCount() { return breaksCount; }
    public void setBreaksCount(int breaksCount) { this.breaksCount = breaksCount; }
    
    public int getShotsCount() { return shotsCount; }
    public void setShotsCount(int shotsCount) { this.shotsCount = shotsCount; }
    
    public long getTimeSpent() { return timeSpent; }
    public void setTimeSpent(long timeSpent) { this.timeSpent = timeSpent; }
    
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    
    public List<Shot> getShots() { return shots; }
    public void setShots(List<Shot> shots) { 
        this.shots = shots;
        calculateStats();
    }
    
    public void addShot(Shot shot) {
        shots.add(shot);
        calculateStats();
    }
    
    public void removeLastShot() {
        if (!shots.isEmpty()) {
            shots.remove(shots.size() - 1);
            calculateStats();
        }
    }
    
    private void calculateStats() {
        shotsCount = shots.size();
        if (shotsCount == 0) {
            totalScore = 0;
            averageScore = 0;
            bestShot = 0;
            worstShot = 11.0;
            breaksCount = 0;
            return;
        }
        
        double sum = 0;
        double best = 0;
        double worst = 11.0;
        int breaks = 0;
        
        for (Shot shot : shots) {
            double score = shot.getScore();
            sum += score;
            
            if (score > best) best = score;
            if (score < worst) worst = score;
            if (score < 9.0) breaks++;
        }
        
        totalScore = sum;
        averageScore = sum / shotsCount;
        bestShot = best;
        worstShot = worst;
        breaksCount = breaks;
    }
    
    public String getFormattedTime() {
        long minutes = timeSpent / 60;
        long seconds = timeSpent % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    public boolean isTest() {
        return "test".equals(type);
    }
}
