package com.shooterpro.app.repository;

import android.content.Context;

import com.shooterpro.app.database.DatabaseHelper;
import com.shooterpro.app.models.Shot;
import com.shooterpro.app.models.TrainingSession;

import java.util.List;

public class TrainingRepository {
    
    private DatabaseHelper dbHelper;
    
    public TrainingRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }
    
    // === Управление спортсменами ===
    public long addShooter(String name) {
        return dbHelper.addShooter(name);
    }
    
    public List<String> getAllShooters() {
        return dbHelper.getAllShooters();
    }
    
    public long getShooterId(String name) {
        return dbHelper.getShooterId(name);
    }
    
    public boolean shooterExists(String name) {
        return dbHelper.shooterExists(name);
    }
    
    // === Управление тренировками ===
    public long saveTraining(TrainingSession session) {
        return dbHelper.addTraining(session);
    }
    
    public TrainingSession getTraining(long shooterId, String date, String type) {
        return dbHelper.getTraining(shooterId, date, type);
    }
    
    public boolean hasTraining(long shooterId, String date, String type) {
        TrainingSession session = getTraining(shooterId, date, type);
        return session != null && session.getShotsCount() > 0;
    }
    
    // === Статистика ===
    public DatabaseHelper.TrainingStats getStatistics(long shooterId) {
        return dbHelper.getStatistics(shooterId);
    }
    
    public DatabaseHelper.TrainingStats getStatistics(String shooterName) {
        long shooterId = getShooterId(shooterName);
        if (shooterId == -1) return new DatabaseHelper.TrainingStats();
        return getStatistics(shooterId);
    }
    
    // === Очистка данных ===
    public void deleteShooterData(String shooterName) {
        dbHelper.deleteShooterData(shooterName);
    }
    
    public void close() {
        dbHelper.close();
    }
}
