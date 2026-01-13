package com.shooterpro.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shooterpro.app.data.ShooterDataManager;
import com.shooterpro.app.utils.DateUtils;
import com.shooterpro.app.utils.DisplayUtils;
import com.shooterpro.app.ui.ButtonFactory;
import com.shooterpro.app.ui.TextViewFactory;

import java.util.Calendar;
import java.util.Locale;

public class DayActivity extends BaseActivity {
    
    private ShooterDataManager dataManager;
    private String currentShooter;
    private int day, month, year;
    private String dateKey;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dataManager = ShooterDataManager.getInstance(this);
        currentShooter = dataManager.getCurrentShooter();
        
        day = getIntent().getIntExtra("day", -1);
        month = getIntent().getIntExtra("month", -1);
        year = getIntent().getIntExtra("year", -1);
        
        if (day == -1 || month == -1 || year == -1) {
            Calendar today = Calendar.getInstance();
            day = today.get(Calendar.DAY_OF_MONTH);
            month = today.get(Calendar.MONTH);
            year = today.get(Calendar.YEAR);
        }
        
        dateKey = DateUtils.formatDate(day, month, year);
        createDayScreen();
    }
    
    private void createDayScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0A192F);
        layout.setGravity(Gravity.CENTER);
        
        int padding = DisplayUtils.dpToPx(this, 16);
        layout.setPadding(padding, DisplayUtils.dpToPx(this, 20), padding, padding);
        
        String dateText = DateUtils.getDateText(day, month);
        TextView dateTitle = TextViewFactory.createTitle(this, "📅 " + dateText);
        layout.addView(dateTitle);
        
        String dayOfWeek = DateUtils.getWeekday(year, month, day);
        TextView dayText = TextViewFactory.createSubtitle(this, dayOfWeek);
        layout.addView(dayText);
        
        // Статус выполнения
        boolean hasTraining = dataManager.hasTraining(currentShooter, dateKey);
        boolean hasTest = dataManager.hasTest(currentShooter, dateKey);
        boolean testCompletedToday = dataManager.isTestCompletedToday(currentShooter);
        
        if (hasTraining && hasTest) {
            TextView status = TextViewFactory.createInfoText(this, "✅ Сегодня уже выполнены: Тренировка и Зачёт");
            status.setTextColor(0xFF4FD1C7);
            layout.addView(status);
        } else if (hasTraining) {
            TextView status = TextViewFactory.createInfoText(this, "✅ Тренировка уже выполнена сегодня");
            status.setTextColor(0xFF9F7AEA);
            layout.addView(status);
        } else if (hasTest) {
            TextView status = TextViewFactory.createInfoText(this, "✅ Зачёт уже пройден сегодня");
            status.setTextColor(0xFFECC94B);
            layout.addView(status);
        }
        
        TextView greeting = TextViewFactory.createSubtitle(this, "Что будем делать сегодня?");
        layout.addView(greeting);
        
        // Кнопки
        Button btnLearning = ButtonFactory.createPrimaryButton(this, "📚 ОБУЧЕНИЕ");
        btnLearning.setOnClickListener(v -> startLearning());
        layout.addView(btnLearning);
        
        Button btnTraining = ButtonFactory.createSecondaryButton(this, "🏁 ТРЕНИРОВКА");
        if (hasTraining) {
            btnTraining.setText("🏁 ТРЕНИРОВКА (уже выполнена)");
        }
        btnTraining.setOnClickListener(v -> startTraining());
        layout.addView(btnTraining);
        
        Button btnTest = ButtonFactory.createWarningButton(this, "🎯 ЗАЧЕТ");
        if (testCompletedToday || hasTest) {
            btnTest.setText("🎯 ЗАЧЕТ (пройден)");
            btnTest.setBackgroundColor(0xFF4A5568);
            btnTest.setEnabled(false);
        }
        btnTest.setOnClickListener(v -> {
            if (!testCompletedToday && !hasTest) {
                startTest();
            } else {
                Toast.makeText(this, "Зачет уже пройден сегодня!", Toast.LENGTH_SHORT).show();
            }
        });
        layout.addView(btnTest);
        
        Button btnNotes = ButtonFactory.createWarningButton(this, "📝 ЗАМЕТКИ");
        btnNotes.setBackgroundColor(0xFFF56565);
        btnNotes.setOnClickListener(v -> startNotes());
        layout.addView(btnNotes);
        
        Button btnBack = ButtonFactory.createBackButton(this, "← НАЗАД");
        btnBack.setOnClickListener(v -> finish());
        layout.addView(btnBack);
        
        setContentView(layout);
    }
    
    private void startLearning() {
        Intent intent = new Intent(this, LearningActivity.class);
        addDateToIntent(intent);
        startActivity(intent);
    }
    
    private void startTraining() {
        if (dataManager.hasActiveTraining(currentShooter)) {
            Intent intent = new Intent(this, TrainingActivity.class);
            intent.putExtra("continue_training", true);
            addDateToIntent(intent);
            startActivity(intent);
        } else {
            // Проверяем настройку последовательных таймеров
            // Просто переходим к тренировке напрямую для простоты
            Intent intent = new Intent(this, TrainingActivity.class);
            intent.putExtra("continue_training", false);
            addDateToIntent(intent);
            startActivity(intent);
        }
    }
    
    private void startTest() {
        if (dataManager.isTestCompletedToday(currentShooter) || 
            dataManager.hasTest(currentShooter, dateKey)) {
            Toast.makeText(this, "Зачет уже пройден сегодня!", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, TestActivity.class);
        addDateToIntent(intent);
        startActivity(intent);
    }
    
    private void startNotes() {
        Intent intent = new Intent(this, NotesActivity.class);
        addDateToIntent(intent);
        startActivity(intent);
    }
    
    private void addDateToIntent(Intent intent) {
        intent.putExtra("day", day);
        intent.putExtra("month", month);
        intent.putExtra("year", year);
    }
}
