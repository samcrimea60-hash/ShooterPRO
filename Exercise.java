package com.shooterpro.app;

public class Exercise {
    private String id;
    private String name;
    private String description;
    private String dayOfWeek;
    private int duration;
    private String category;
    private String difficulty;

    public Exercise(String id, String name, String description, String dayOfWeek, 
					int duration, String category, String difficulty) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.dayOfWeek = dayOfWeek;
        this.duration = duration;
        this.category = category;
        this.difficulty = difficulty;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getDayOfWeek() { return dayOfWeek; }
    public int getDuration() { return duration; }
    public String getCategory() { return category; }
    public String getDifficulty() { return difficulty; }

    public int getCategoryColor() {
        switch (category) {
            case "Разминка": return 0xFF4FD1C7;
            case "Техника": return 0xFF9F7AEA;
            case "Стабильность": return 0xFFECC94B;
            case "Сила": return 0xFFF56565;
            case "Завершение": return 0xFF48BB78;
            default: return 0xFFA0AEC0;
        }
    }

    public String getDayIcon() {
        switch (dayOfWeek) {
            case "ПН": return "①";
            case "ВТ": return "②";
            case "СР": return "③";
            case "ЧТ": return "④";
            case "ПТ": return "⑤";
            case "СБ": return "⑥";
            case "ВС": return "⑦";
            default: return "⚪";
        }
    }
}
