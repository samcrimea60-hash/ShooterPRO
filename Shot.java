package com.shooterpro.app.models;

import java.text.DecimalFormat;

public class Shot {
    private float score;
    private long timestamp;
    private int seriesNumber;
    
    private static final DecimalFormat df = new DecimalFormat("0.0");
    
    public Shot(float score, long timestamp, int seriesNumber) {
        this.score = Math.round(score * 10) / 10.0f;
        this.timestamp = timestamp;
        this.seriesNumber = seriesNumber;
    }
    
    public float getScore() { return score; }
    public long getTimestamp() { return timestamp; }
    public int getSeriesNumber() { return seriesNumber; }
    
    public String getFormattedScore() {
        return df.format(score);
    }
    
    public boolean isBreak() {
        return score < 9.0f;
    }
    
    public boolean isExcellent() {
        return score >= 10.0f;
    }
    
    public String getQuality() {
        if (score >= 10.0f) return "Отлично";
        if (score >= 9.0f) return "Хорошо";
        if (score >= 8.0f) return "Удовлетворительно";
        return "Отрыв";
    }
}
