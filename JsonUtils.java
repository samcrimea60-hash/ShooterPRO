package com.shooterpro.app.utils;

import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    
    public static String createJsonArray(List<Float> values) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        
        for (int i = 0; i < values.size(); i++) {
            sb.append(String.format("%.1f", values.get(i)));
            if (i < values.size() - 1) {
                sb.append(",");
            }
        }
        
        sb.append("]");
        return sb.toString();
    }
    
    public static List<Float> parseJsonArray(String jsonArray) {
        List<Float> result = new ArrayList<>();
        
        if (jsonArray == null || jsonArray.length() < 2) {
            return result;
        }
        
        // Убираем скобки
        String content = jsonArray.substring(1, jsonArray.length() - 1);
        String[] parts = content.split(",");
        
        for (String part : parts) {
            try {
                result.add(Float.parseFloat(part.trim()));
            } catch (NumberFormatException e) {
                // Пропускаем некорректные значения
            }
        }
        
        return result;
    }
    
    public static String createTrainingJson(List<Float> shots, long timeSpent, String notes) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"shots\":").append(createJsonArray(shots)).append(",");
        sb.append("\"time_spent\":").append(timeSpent).append(",");
        sb.append("\"notes\":\"").append(notes != null ? notes : "").append("\"");
        sb.append("}");
        return sb.toString();
    }
    
    public static TrainingData parseTrainingJson(String json) {
        TrainingData data = new TrainingData();
        
        if (json == null || json.isEmpty()) {
            return data;
        }
        
        try {
            // Простой парсинг JSON
            int shotsStart = json.indexOf("\"shots\":") + 8;
            int shotsEnd = json.indexOf("]", shotsStart) + 1;
            String shotsArray = json.substring(shotsStart, shotsEnd);
            data.shots = parseJsonArray(shotsArray);
            
            // Парсим время
            int timeStart = json.indexOf("\"time_spent\":") + 13;
            int timeEnd = json.indexOf(",", timeStart);
            if (timeEnd == -1) timeEnd = json.indexOf("}", timeStart);
            String timeStr = json.substring(timeStart, timeEnd);
            data.timeSpent = Long.parseLong(timeStr.trim());
            
            // Парсим заметки
            if (json.contains("\"notes\":")) {
                int notesStart = json.indexOf("\"notes\":\"") + 9;
                int notesEnd = json.indexOf("\"", notesStart);
                data.notes = json.substring(notesStart, notesEnd);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return data;
    }
    
    public static class TrainingData {
        public List<Float> shots = new ArrayList<>();
        public long timeSpent = 0;
        public String notes = "";
    }
}
