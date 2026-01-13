package com.shooterpro.app;

import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shooterpro.app.data.ShooterDataManager;
import com.shooterpro.app.utils.DateUtils;
import com.shooterpro.app.utils.DisplayUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

public class TrainingActivity extends BaseActivity {
    
    private ShooterDataManager dataManager;
    private String currentShooter, dateKey;
    private ArrayList<Float> shotsList = new ArrayList<>();
    private long startTime;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private TextView timerView, totalShotsView;
    private EditText inputScore;
    private DecimalFormat df = new DecimalFormat("0.0");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dataManager = ShooterDataManager.getInstance(this);
        currentShooter = dataManager.getCurrentShooter();
        
        int day = getIntent().getIntExtra("day", 1);
        int month = getIntent().getIntExtra("month", 0);
        int year = getIntent().getIntExtra("year", 2024);
        dateKey = DateUtils.formatDate(day, month + 1, year);
        
        boolean continueTraining = getIntent().getBooleanExtra("continue_training", false);
        
        if (continueTraining) {
            loadTrainingData();
        } else {
            startTime = System.currentTimeMillis();
            dataManager.setActiveTraining(currentShooter, true);
        }
        
        createTrainingScreen();
        startTimer();
    }
    
    private void createTrainingScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);
        
        int padding = DisplayUtils.dpToPx(this, 16);
        mainLayout.setPadding(padding, padding, padding, padding);
        
        // Заголовок
        TextView title = new TextView(this);
        title.setText("🏁 ТРЕНИРОВКА");
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        mainLayout.addView(title);
        
        // Таймер
        timerView = new TextView(this);
        timerView.setText("00:00");
        timerView.setTextColor(0xFFE2E8F0);
        timerView.setTextSize(16);
        timerView.setGravity(Gravity.CENTER);
        timerView.setPadding(0, 0, 0, DisplayUtils.dpToPx(this, 16));
        mainLayout.addView(timerView);
        
        // Блок ввода
        LinearLayout inputBlock = new LinearLayout(this);
        inputBlock.setOrientation(LinearLayout.VERTICAL);
        inputBlock.setBackgroundColor(0xFF2A3B5A);
        inputBlock.setPadding(padding, padding, padding, padding);
        
        TextView inputTitle = new TextView(this);
        inputTitle.setText("ВВОД РЕЗУЛЬТАТА");
        inputTitle.setTextColor(0xFFE2E8F0);
        inputTitle.setTextSize(14);
        inputTitle.setGravity(Gravity.CENTER);
        inputBlock.addView(inputTitle);
        
        inputScore = new EditText(this);
        inputScore.setText("10.9");
        inputScore.setTextColor(0xFF4FD1C7);
        inputScore.setTextSize(22);
        inputScore.setGravity(Gravity.CENTER);
        inputScore.setBackgroundColor(0xFF1A2B4A);
        inputScore.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 50)
        );
        params.setMargins(0, DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8));
        inputScore.setLayoutParams(params);
        inputBlock.addView(inputScore);
        
        TextView hint = new TextView(this);
        hint.setText("Введите от 0.0 до 10.9");
        hint.setTextColor(0xFFA0AEC0);
        hint.setTextSize(12);
        hint.setGravity(Gravity.CENTER);
        inputBlock.addView(hint);
        
        Button addBtn = new Button(this);
        addBtn.setText("ДОБАВИТЬ ВЫСТРЕЛ");
        addBtn.setBackgroundColor(0xFF4FD1C7);
        addBtn.setTextColor(0xFFFFFFFF);
        addBtn.setTextSize(14);
        addBtn.setOnClickListener(v -> addShot());
        
        params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 52)
        );
        params.setMargins(0, DisplayUtils.dpToPx(this, 8), 0, 0);
        addBtn.setLayoutParams(params);
        inputBlock.addView(addBtn);
        
        mainLayout.addView(inputBlock);
        
        // Статистика
        LinearLayout statsBlock = new LinearLayout(this);
        statsBlock.setOrientation(LinearLayout.VERTICAL);
        statsBlock.setBackgroundColor(0xFF2A3B5A);
        statsBlock.setPadding(padding, padding, padding, padding);
        
        TextView statsTitle = new TextView(this);
        statsTitle.setText("СТАТИСТИКА");
        statsTitle.setTextColor(0xFFE2E8F0);
        statsTitle.setTextSize(14);
        statsTitle.setGravity(Gravity.CENTER);
        statsBlock.addView(statsTitle);
        
        LinearLayout shotsRow = createStatRow("Выстрелов:", "0");
        totalShotsView = (TextView) shotsRow.getChildAt(1);
        statsBlock.addView(shotsRow);
        
        statsBlock.addView(createStatRow("Средний балл:", "0.0"));
        statsBlock.addView(createStatRow("Лучший выстрел:", "0.0"));
        statsBlock.addView(createStatRow("Худший выстрел:", "0.0"));
        statsBlock.addView(createStatRow("Отрывов (<9.0):", "0"));
        
        params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, DisplayUtils.dpToPx(this, 16), 0, 0);
        statsBlock.setLayoutParams(params);
        mainLayout.addView(statsBlock);
        
        // Кнопки управления
        LinearLayout buttonsBlock = new LinearLayout(this);
        buttonsBlock.setOrientation(LinearLayout.HORIZONTAL);
        buttonsBlock.setPadding(0, DisplayUtils.dpToPx(this, 20), 0, 0);
        
        Button deleteBtn = createActionButton("УДАЛИТЬ", 0xFF4A5568);
        deleteBtn.setOnClickListener(v -> deleteLastShot());
        buttonsBlock.addView(deleteBtn);
        
        Button finishBtn = createActionButton("ЗАВЕРШИТЬ", 0xFF4FD1C7);
        finishBtn.setOnClickListener(v -> finishTraining());
        buttonsBlock.addView(finishBtn);
        
        mainLayout.addView(buttonsBlock);
        
        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
    
    private LinearLayout createStatRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8));
        
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFFA0AEC0);
        labelView.setTextSize(12);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
        row.addView(labelView);
        
        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFE2E8F0);
        valueView.setTextSize(12);
        valueView.setGravity(Gravity.END);
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
        row.addView(valueView);
        
        return row;
    }
    
    private Button createActionButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(12);
        button.setPadding(0, DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                                                                         DisplayUtils.dpToPx(this, 52), 1);
        params.setMargins(DisplayUtils.dpToPx(this, 2), 0, DisplayUtils.dpToPx(this, 2), 0);
        button.setLayoutParams(params);
        
        return button;
    }
    
    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                long minutes = elapsed / 60;
                long seconds = elapsed % 60;
                timerView.setText(String.format("%02d:%02d", minutes, seconds));
                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }
    
    private void addShot() {
        String input = inputScore.getText().toString().trim().replace(',', '.');
        if (input.isEmpty()) {
            Toast.makeText(this, "Введите результат!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            float score = Float.parseFloat(input);
            if (score < 0.0f || score > 10.9f) {
                Toast.makeText(this, "Введите от 0.0 до 10.9", Toast.LENGTH_SHORT).show();
                return;
            }
            
            score = Math.round(score * 10) / 10.0f;
            shotsList.add(score);
            
            saveTrainingData();
            updateDisplay();
            
            Toast.makeText(this, "Выстрел " + shotsList.size() + ": " + df.format(score),
                           Toast.LENGTH_SHORT).show();
            
            inputScore.setText("");
            inputScore.requestFocus();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ошибка! Введите число", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void deleteLastShot() {
        if (!shotsList.isEmpty()) {
            shotsList.remove(shotsList.size() - 1);
            saveTrainingData();
            updateDisplay();
            Toast.makeText(this, "Последний выстрел удален", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void finishTraining() {
        saveTrainingData();
        dataManager.setActiveTraining(currentShooter, false);
        
        // Сохраняем факт тренировки
        try {
            JSONObject stats = new JSONObject();
            float totalScore = 0;
            for (float shot : shotsList) {
                totalScore += shot;
            }
            dataManager.updateStatistics(currentShooter, totalScore, shotsList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Toast.makeText(this, "Тренировка завершена!", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void updateDisplay() {
        if (totalShotsView != null) {
            totalShotsView.setText(String.valueOf(shotsList.size()));
        }
        
        if (!shotsList.isEmpty()) {
            float sum = 0, best = 0, worst = 11.0f;
            int breaks = 0;
            
            for (float shot : shotsList) {
                sum += shot;
                if (shot > best) best = shot;
                if (shot < worst) worst = shot;
                if (shot < 9.0f) breaks++;
            }
            
            updateStatValue(1, df.format(sum / shotsList.size()));
            updateStatValue(2, df.format(best));
            updateStatValue(3, df.format(worst));
            updateStatValue(4, String.valueOf(breaks));
        } else {
            updateStatValue(1, "0.0");
            updateStatValue(2, "0.0");
            updateStatValue(3, "0.0");
            updateStatValue(4, "0");
        }
    }
    
    private void updateStatValue(int index, String value) {
        // Упрощенная реализация для AIDE
        // В реальном приложении нужно использовать ViewHolder паттерн
    }
    
    private void saveTrainingData() {
        try {
            JSONObject trainingData = new JSONObject();
            JSONArray shotsArray = new JSONArray();
            for (float shot : shotsList) {
                shotsArray.put(shot);
            }
            trainingData.put("shots", shotsArray);
            trainingData.put("start_time", startTime);
            
            dataManager.saveTrainingData(currentShooter, dateKey, trainingData);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    private void loadTrainingData() {
        JSONObject data = dataManager.getTrainingData(currentShooter, dateKey);
        if (data.length() > 0) {
            try {
                JSONArray shotsArray = data.getJSONArray("shots");
                shotsList.clear();
                for (int i = 0; i < shotsArray.length(); i++) {
                    shotsList.add((float) shotsArray.getDouble(i));
                }
                startTime = data.getLong("start_time");
            } catch (JSONException e) {
                e.printStackTrace();
                startTime = System.currentTimeMillis();
            }
        } else {
            startTime = System.currentTimeMillis();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
