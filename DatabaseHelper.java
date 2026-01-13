package com.shooterpro.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.shooterpro.app.models.Shot;
import com.shooterpro.app.models.TrainingSession;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "shooterpro.db";
    private static final int DATABASE_VERSION = 4;
    
    // Таблица спортсменов
    private static final String TABLE_SHOOTERS = "shooters";
    private static final String COLUMN_SHOOTER_ID = "shooter_id";
    private static final String COLUMN_SHOOTER_NAME = "name";
    private static final String COLUMN_CREATED_AT = "created_at";
    
    // Таблица тренировок
    private static final String TABLE_TRAININGS = "trainings";
    private static final String COLUMN_TRAINING_ID = "training_id";
    private static final String COLUMN_DATE = "date";
    private static final String COLUMN_TYPE = "type"; // "training" или "test"
    private static final String COLUMN_TOTAL_SCORE = "total_score";
    private static final String COLUMN_AVERAGE_SCORE = "average_score";
    private static final String COLUMN_BEST_SHOT = "best_shot";
    private static final String COLUMN_WORST_SHOT = "worst_shot";
    private static final String COLUMN_BREAKS_COUNT = "breaks_count";
    private static final String COLUMN_SHOTS_COUNT = "shots_count";
    private static final String COLUMN_TIME_SPENT = "time_spent";
    private static final String COLUMN_NOTES = "notes";
    
    // Таблица выстрелов
    private static final String TABLE_SHOTS = "shots";
    private static final String COLUMN_SHOT_ID = "shot_id";
    private static final String COLUMN_SHOT_SCORE = "score";
    private static final String COLUMN_SERIES_NUMBER = "series_number";
    private static final String COLUMN_SHOT_TIME = "shot_time";
    
    // Таблица серий
    private static final String TABLE_SERIES = "series";
    private static final String COLUMN_SERIES_ID = "series_id";
    private static final String COLUMN_SERIES_TOTAL = "series_total";
    
    // Таблица статистики
    private static final String TABLE_STATS = "statistics";
    private static final String COLUMN_STATS_ID = "stats_id";
    private static final String COLUMN_TOTAL_TRAININGS = "total_trainings";
    private static final String COLUMN_TOTAL_SHOTS = "total_shots";
    private static final String COLUMN_OVERALL_AVERAGE = "overall_average";
    private static final String COLUMN_LAST_TRAINING = "last_training";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        createShootersTable(db);
        createTrainingsTable(db);
        createShotsTable(db);
        createSeriesTable(db);
        createStatsTable(db);
    }
    
    private void createShootersTable(SQLiteDatabase db) {
        String CREATE_SHOOTERS_TABLE = "CREATE TABLE " + TABLE_SHOOTERS + "("
                + COLUMN_SHOOTER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SHOOTER_NAME + " TEXT UNIQUE NOT NULL,"
                + COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_SHOOTERS_TABLE);
    }
    
    private void createTrainingsTable(SQLiteDatabase db) {
        String CREATE_TRAININGS_TABLE = "CREATE TABLE " + TABLE_TRAININGS + "("
                + COLUMN_TRAINING_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SHOOTER_ID + " INTEGER NOT NULL,"
                + COLUMN_DATE + " TEXT NOT NULL,"
                + COLUMN_TYPE + " TEXT NOT NULL,"
                + COLUMN_TOTAL_SCORE + " REAL DEFAULT 0,"
                + COLUMN_AVERAGE_SCORE + " REAL DEFAULT 0,"
                + COLUMN_BEST_SHOT + " REAL DEFAULT 0,"
                + COLUMN_WORST_SHOT + " REAL DEFAULT 11.0,"
                + COLUMN_BREAKS_COUNT + " INTEGER DEFAULT 0,"
                + COLUMN_SHOTS_COUNT + " INTEGER DEFAULT 0,"
                + COLUMN_TIME_SPENT + " INTEGER DEFAULT 0,"
                + COLUMN_NOTES + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_SHOOTER_ID + ") REFERENCES " + TABLE_SHOOTERS + "(" + COLUMN_SHOOTER_ID + ")"
                + ")";
        db.execSQL(CREATE_TRAININGS_TABLE);
    }
    
    private void createShotsTable(SQLiteDatabase db) {
        String CREATE_SHOTS_TABLE = "CREATE TABLE " + TABLE_SHOTS + "("
                + COLUMN_SHOT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TRAINING_ID + " INTEGER NOT NULL,"
                + COLUMN_SHOT_SCORE + " REAL NOT NULL,"
                + COLUMN_SERIES_NUMBER + " INTEGER DEFAULT 1,"
                + COLUMN_SHOT_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY(" + COLUMN_TRAINING_ID + ") REFERENCES " + TABLE_TRAININGS + "(" + COLUMN_TRAINING_ID + ")"
                + ")";
        db.execSQL(CREATE_SHOTS_TABLE);
    }
    
    private void createSeriesTable(SQLiteDatabase db) {
        String CREATE_SERIES_TABLE = "CREATE TABLE " + TABLE_SERIES + "("
                + COLUMN_SERIES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TRAINING_ID + " INTEGER NOT NULL,"
                + COLUMN_SERIES_NUMBER + " INTEGER NOT NULL,"
                + COLUMN_SERIES_TOTAL + " REAL DEFAULT 0,"
                + "FOREIGN KEY(" + COLUMN_TRAINING_ID + ") REFERENCES " + TABLE_TRAININGS + "(" + COLUMN_TRAINING_ID + ")"
                + ")";
        db.execSQL(CREATE_SERIES_TABLE);
    }
    
    private void createStatsTable(SQLiteDatabase db) {
        String CREATE_STATS_TABLE = "CREATE TABLE " + TABLE_STATS + "("
                + COLUMN_STATS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_SHOOTER_ID + " INTEGER UNIQUE NOT NULL,"
                + COLUMN_TOTAL_TRAININGS + " INTEGER DEFAULT 0,"
                + COLUMN_TOTAL_SHOTS + " INTEGER DEFAULT 0,"
                + COLUMN_OVERALL_AVERAGE + " REAL DEFAULT 0,"
                + COLUMN_LAST_TRAINING + " TEXT,"
                + "FOREIGN KEY(" + COLUMN_SHOOTER_ID + ") REFERENCES " + TABLE_SHOOTERS + "(" + COLUMN_SHOOTER_ID + ")"
                + ")";
        db.execSQL(CREATE_STATS_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAININGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STATS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOOTERS);
        onCreate(db);
    }
    
    // === CRUD операции для спортсменов ===
    public long addShooter(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SHOOTER_NAME, name);
        
        long id = db.insert(TABLE_SHOOTERS, null, values);
        db.close();
        return id;
    }
    
    public List<String> getAllShooters() {
        List<String> shooters = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_SHOOTERS + " ORDER BY " + COLUMN_SHOOTER_NAME;
        Cursor cursor = db.rawQuery(query, null);
        
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SHOOTER_NAME));
                shooters.add(name);
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return shooters;
    }
    
    public long getShooterId(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        long id = -1;
        
        String query = "SELECT " + COLUMN_SHOOTER_ID + " FROM " + TABLE_SHOOTERS + 
                      " WHERE " + COLUMN_SHOOTER_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SHOOTER_ID));
        }
        
        cursor.close();
        db.close();
        return id;
    }
    
    public boolean shooterExists(String name) {
        return getShooterId(name) != -1;
    }
    
    // === CRUD операции для тренировок ===
    public long addTraining(TrainingSession session) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_SHOOTER_ID, session.getShooterId());
        values.put(COLUMN_DATE, session.getDate());
        values.put(COLUMN_TYPE, session.getType());
        values.put(COLUMN_TOTAL_SCORE, session.getTotalScore());
        values.put(COLUMN_AVERAGE_SCORE, session.getAverageScore());
        values.put(COLUMN_BEST_SHOT, session.getBestShot());
        values.put(COLUMN_WORST_SHOT, session.getWorstShot());
        values.put(COLUMN_BREAKS_COUNT, session.getBreaksCount());
        values.put(COLUMN_SHOTS_COUNT, session.getShotsCount());
        values.put(COLUMN_TIME_SPENT, session.getTimeSpent());
        values.put(COLUMN_NOTES, session.getNotes());
        
        long trainingId = db.insert(TABLE_TRAININGS, null, values);
        
        // Добавляем выстрелы
        for (Shot shot : session.getShots()) {
            addShot(trainingId, shot);
        }
        
        // Обновляем статистику
        updateStatistics(session.getShooterId(), session);
        
        db.close();
        return trainingId;
    }
    
    private long addShot(long trainingId, Shot shot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_TRAINING_ID, trainingId);
        values.put(COLUMN_SHOT_SCORE, shot.getScore());
        values.put(COLUMN_SERIES_NUMBER, shot.getSeriesNumber());
        
        long shotId = db.insert(TABLE_SHOTS, null, values);
        db.close();
        return shotId;
    }
    
    public TrainingSession getTraining(long shooterId, String date, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        TrainingSession session = null;
        
        String query = "SELECT * FROM " + TABLE_TRAININGS + 
                      " WHERE " + COLUMN_SHOOTER_ID + " = ? AND " + 
                      COLUMN_DATE + " = ? AND " + COLUMN_TYPE + " = ?";
        
        Cursor cursor = db.rawQuery(query, new String[]{
            String.valueOf(shooterId), date, type
        });
        
        if (cursor.moveToFirst()) {
            session = cursorToTrainingSession(cursor);
            session.setShots(getShotsForTraining(session.getId()));
        }
        
        cursor.close();
        db.close();
        return session;
    }
    
    private List<Shot> getShotsForTraining(long trainingId) {
        List<Shot> shots = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String query = "SELECT * FROM " + TABLE_SHOTS + 
                      " WHERE " + COLUMN_TRAINING_ID + " = ?" +
                      " ORDER BY " + COLUMN_SHOT_ID;
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(trainingId)});
        
        if (cursor.moveToFirst()) {
            do {
                float score = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SHOT_SCORE));
                int series = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SERIES_NUMBER));
                long time = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SHOT_TIME));
                
                shots.add(new Shot(score, time, series));
            } while (cursor.moveToNext());
        }
        
        cursor.close();
        db.close();
        return shots;
    }
    
    private TrainingSession cursorToTrainingSession(Cursor cursor) {
        TrainingSession session = new TrainingSession();
        
        session.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRAINING_ID)));
        session.setShooterId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_SHOOTER_ID)));
        session.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
        session.setType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)));
        session.setTotalScore(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SCORE)));
        session.setAverageScore(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AVERAGE_SCORE)));
        session.setBestShot(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BEST_SHOT)));
        session.setWorstShot(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_WORST_SHOT)));
        session.setBreaksCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BREAKS_COUNT)));
        session.setShotsCount(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SHOTS_COUNT)));
        session.setTimeSpent(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIME_SPENT)));
        session.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTES)));
        
        return session;
    }
    
    // === Статистика ===
    private void updateStatistics(long shooterId, TrainingSession session) {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Проверяем, есть ли уже статистика
        String checkQuery = "SELECT * FROM " + TABLE_STATS + 
                           " WHERE " + COLUMN_SHOOTER_ID + " = ?";
        Cursor cursor = db.rawQuery(checkQuery, new String[]{String.valueOf(shooterId)});
        
        if (cursor.moveToFirst()) {
            // Обновляем существующую статистику
            updateExistingStats(db, shooterId, session);
        } else {
            // Создаем новую запись статистики
            createNewStats(db, shooterId, session);
        }
        
        cursor.close();
        db.close();
    }
    
    private void updateExistingStats(SQLiteDatabase db, long shooterId, TrainingSession session) {
        ContentValues values = new ContentValues();
        
        // Получаем текущие значения
        String query = "SELECT * FROM " + TABLE_STATS + 
                      " WHERE " + COLUMN_SHOOTER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(shooterId)});
        
        if (cursor.moveToFirst()) {
            int totalTrainings = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_TRAININGS));
            int totalShots = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SHOTS));
            double overallAvg = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OVERALL_AVERAGE));
            
            // Вычисляем новое среднее
            double newAvg = (overallAvg * totalTrainings + session.getAverageScore()) / (totalTrainings + 1);
            
            values.put(COLUMN_TOTAL_TRAININGS, totalTrainings + 1);
            values.put(COLUMN_TOTAL_SHOTS, totalShots + session.getShotsCount());
            values.put(COLUMN_OVERALL_AVERAGE, newAvg);
            values.put(COLUMN_LAST_TRAINING, session.getDate());
            
            db.update(TABLE_STATS, values, COLUMN_SHOOTER_ID + " = ?", 
                     new String[]{String.valueOf(shooterId)});
        }
        
        cursor.close();
    }
    
    private void createNewStats(SQLiteDatabase db, long shooterId, TrainingSession session) {
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_SHOOTER_ID, shooterId);
        values.put(COLUMN_TOTAL_TRAININGS, 1);
        values.put(COLUMN_TOTAL_SHOTS, session.getShotsCount());
        values.put(COLUMN_OVERALL_AVERAGE, session.getAverageScore());
        values.put(COLUMN_LAST_TRAINING, session.getDate());
        
        db.insert(TABLE_STATS, null, values);
    }
    
    public TrainingStats getStatistics(long shooterId) {
        SQLiteDatabase db = this.getReadableDatabase();
        TrainingStats stats = new TrainingStats();
        
        String query = "SELECT * FROM " + TABLE_STATS + 
                      " WHERE " + COLUMN_SHOOTER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(shooterId)});
        
        if (cursor.moveToFirst()) {
            stats.setTotalTrainings(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_TRAININGS)));
            stats.setTotalShots(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TOTAL_SHOTS)));
            stats.setOverallAverage(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_OVERALL_AVERAGE)));
            stats.setLastTraining(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_TRAINING)));
        }
        
        cursor.close();
        db.close();
        return stats;
    }
    
    // === Удаление данных ===
    public void deleteShooterData(String shooterName) {
        long shooterId = getShooterId(shooterName);
        if (shooterId == -1) return;
        
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Получаем все тренировки стрелка
        String getTrainingsQuery = "SELECT " + COLUMN_TRAINING_ID + " FROM " + TABLE_TRAININGS +
                                  " WHERE " + COLUMN_SHOOTER_ID + " = ?";
        Cursor cursor = db.rawQuery(getTrainingsQuery, new String[]{String.valueOf(shooterId)});
        
        List<Long> trainingIds = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                trainingIds.add(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRAINING_ID)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        
        // Удаляем выстрелы для каждой тренировки
        for (Long trainingId : trainingIds) {
            db.delete(TABLE_SHOTS, COLUMN_TRAINING_ID + " = ?", 
                     new String[]{String.valueOf(trainingId)});
        }
        
        // Удаляем тренировки
        db.delete(TABLE_TRAININGS, COLUMN_SHOOTER_ID + " = ?", 
                 new String[]{String.valueOf(shooterId)});
        
        // Удаляем статистику
        db.delete(TABLE_STATS, COLUMN_SHOOTER_ID + " = ?", 
                 new String[]{String.valueOf(shooterId)});
        
        // Удаляем стрелка
        db.delete(TABLE_SHOOTERS, COLUMN_SHOOTER_ID + " = ?", 
                 new String[]{String.valueOf(shooterId)});
        
        db.close();
    }
    
    // Вспомогательный класс для статистики
    public static class TrainingStats {
        private int totalTrainings;
        private int totalShots;
        private double overallAverage;
        private String lastTraining;
        
        public int getTotalTrainings() { return totalTrainings; }
        public void setTotalTrainings(int totalTrainings) { this.totalTrainings = totalTrainings; }
        
        public int getTotalShots() { return totalShots; }
        public void setTotalShots(int totalShots) { this.totalShots = totalShots; }
        
        public double getOverallAverage() { return overallAverage; }
        public void setOverallAverage(double overallAverage) { this.overallAverage = overallAverage; }
        
        public String getLastTraining() { return lastTraining; }
        public void setLastTraining(String lastTraining) { this.lastTraining = lastTraining; }
    }
}
