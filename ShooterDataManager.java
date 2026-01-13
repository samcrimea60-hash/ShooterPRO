package com.shooterpro.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.shooterpro.app.database.DatabaseHelper;
import com.shooterpro.app.models.TrainingSession;
import com.shooterpro.app.repository.TrainingRepository;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ShooterDataManager {
    
    private static final String PREFS_NAME = "ShooterPRO";
    private static ShooterDataManager instance;
    private final SharedPreferences prefs;
    private final TrainingRepository repository;
    private final Context context;
    
    private ShooterDataManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        repository = new TrainingRepository(context);
    }
    
    public static synchronized ShooterDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new ShooterDataManager(context);
        }
        return instance;
    }
    
    // Стрелки
    public String getCurrentShooter() {
        return prefs.getString("current_shooter", "");
    }
    
    public void setCurrentShooter(String shooter) {
        // Сохраняем в базу данных, если еще нет
        if (!repository.shooterExists(shooter)) {
            repository.addShooter(shooter);
        }
        
        // Сохраняем в SharedPreferences для быстрого доступа
        prefs.edit().putString("current_shooter", shooter).apply();
        
        // Сохраняем в список стрелков
        Set<String> currentShooters = prefs.getStringSet("shooters_list", new HashSet<>());
        Set<String> updatedShooters = new HashSet<>(currentShooters);
        updatedShooters.add(shooter);
        prefs.edit().putStringSet("shooters_list", updatedShooters).apply();
    }
    
    public List<String> getAllShooters() {
        // Теперь получаем из базы данных
        return repository.getAllShooters();
    }
    
    public void addShooter(String shooter) {
        repository.addShooter(shooter);
        setCurrentShooter(shooter);
    }
    
    // Тренировки
    public void saveTrainingData(String shooter, String dateKey, JSONObject data) {
        try {
            long shooterId = repository.getShooterId(shooter);
            if (shooterId == -1) {
                shooterId = repository.addShooter(shooter);
            }
            
            TrainingSession session = jsonToTrainingSession(shooterId, dateKey, "training", data);
            repository.saveTraining(session);
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public JSONObject getTrainingData(String shooter, String dateKey) {
        long shooterId = repository.getShooterId(shooter);
        if (shooterId == -1) return new JSONObject();
        
        TrainingSession session = repository.getTraining(shooterId, dateKey, "training");
        return trainingSessionToJson(session);
    }
    
    public boolean hasTraining(String shooter, String dateKey) {
        long shooterId = repository.getShooterId(shooter);
        if (shooterId == -1) return false;
        
        return repository.hasTraining(shooterId, dateKey, "training");
    }
    
    // Зачеты
    public void saveTestData(String shooter, String dateKey, JSONObject data) {
        try {
            long shooterId = repository.getShooterId(shooter);
            if (shooterId == -1) {
                shooterId = repository.addShooter(shooter);
            }
            
            TrainingSession session = jsonToTrainingSession(shooterId, dateKey, "test", data);
            repository.saveTraining(session);
            
            // Отмечаем как пройденный сегодня
            String todayKey = getTodayKey();
            prefs.edit().putBoolean(shooter + "_" + todayKey + "_test_completed", true).apply();
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    public JSONObject getTestData(String shooter, String dateKey) {
        long shooterId = repository.getShooterId(shooter);
        if (shooterId == -1) return new JSONObject();
        
        TrainingSession session = repository.getTraining(shooterId, dateKey, "test");
        return trainingSessionToJson(session);
    }
    
    public boolean hasTest(String shooter, String dateKey) {
        long shooterId = repository.getShooterId(shooter);
        if (shooterId == -1) return false;
        
        return repository.hasTraining(shooterId, dateKey, "test");
    }
    
    // Статистика
    public DatabaseHelper.TrainingStats getStatistics(String shooter) {
        return repository.getStatistics(shooter);
    }
    
    public void updateStatistics(String shooter, float score, int shots) {
        // Автоматически обновляется в базе данных при сохранении тренировки
    }
    
    // Вспомогательные методы
    private String getTodayKey() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return sdf.format(new Date());
    }
    
    public boolean isTestCompletedToday(String shooter) {
        String todayKey = getTodayKey();
        return prefs.getBoolean(shooter + "_" + todayKey + "_test_completed", false);
    }
    
    // Активная тренировка
    public void setActiveTraining(String shooter, boolean active) {
        prefs.edit().putBoolean(shooter + "_active_training", active).apply();
    }
    
    public boolean hasActiveTraining(String shooter) {
        return prefs.getBoolean(shooter + "_active_training", false);
    }
    
    // Заметки
    public void saveNotes(String shooter, String dateKey, String notes) {
        // Сохраняем в SharedPreferences для простоты
        prefs.edit().putString(shooter + "_" + dateKey + "_notes", notes).apply();
    }
    
    public String getNotes(String shooter, String dateKey) {
        return prefs.getString(shooter + "_" + dateKey + "_notes", "");
    }
    
    // Настройки таймеров
    public void saveCustomTimers(List<String> timers) {
        StringBuilder sb = new StringBuilder();
        for (String timer : timers) {
            sb.append(timer).append(";");
        }
        prefs.edit().putString("custom_timers", sb.toString()).apply();
    }
    
    public List<String> getCustomTimers() {
        String timersStr = prefs.getString("custom_timers", "");
        List<String> timers = new java.util.ArrayList<>();
        if (!timersStr.isEmpty()) {
            String[] parts = timersStr.split(";");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    timers.add(part);
                }
            }
        }
        return timers;
    }
    
    // Настройки напоминаний
    public boolean isRemindersEnabled() {
        return prefs.getBoolean("reminders_enabled", false);
    }
    
    public void setRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean("reminders_enabled", enabled).apply();
    }
    
    public int getReminderHour() {
        return prefs.getInt("reminder_hour", 19);
    }
    
    public int getReminderMinute() {
        return prefs.getInt("reminder_minute", 0);
    }
    
    public void setReminderTime(int hour, int minute) {
        prefs.edit()
            .putInt("reminder_hour", hour)
            .putInt("reminder_minute", minute)
            .apply();
    }
    
    // Очистка данных
    public void clearShooterData(String shooter) {
        repository.deleteShooterData(shooter);
        // Очищаем SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : prefs.getAll().keySet()) {
            if (key.startsWith(shooter + "_")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }
    
    public void clearAllData() {
        prefs.edit().clear().apply();
        // Очистка базы данных (при переустановке приложения)
        // repository.clearAllData(); // Нужно реализовать в репозитории
    }
    
    // Конвертеры
    private TrainingSession jsonToTrainingSession(long shooterId, String date, String type, JSONObject json) 
            throws JSONException {
        TrainingSession session = new TrainingSession(shooterId, date, type);
        
        if (json.has("shots")) {
            JSONArray shotsArray = json.getJSONArray("shots");
            for (int i = 0; i < shotsArray.length(); i++) {
                float score = (float) shotsArray.getDouble(i);
                // Здесь можно создать Shot объект
            }
        }
        
        if (json.has("time_spent")) {
            session.setTimeSpent(json.getLong("time_spent"));
        }
        
        return session;
    }
    
    private JSONObject trainingSessionToJson(TrainingSession session) {
        if (session == null) return new JSONObject();
        
        JSONObject json = new JSONObject();
        try {
            JSONArray shotsArray = new JSONArray();
            for (com.shooterpro.app.models.Shot shot : session.getShots()) {
                shotsArray.put(shot.getScore());
            }
            
            json.put("shots", shotsArray);
            json.put("total_score", session.getTotalScore());
            json.put("average_score", session.getAverageScore());
            json.put("best_shot", session.getBestShot());
            json.put("worst_shot", session.getWorstShot());
            json.put("breaks_count", session.getBreaksCount());
            json.put("shots_count", session.getShotsCount());
            json.put("time_spent", session.getTimeSpent());
            json.put("notes", session.getNotes());
            
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        return json;
    }
    
    public void close() {
        repository.close();
    }
}
