package com.shooterpro.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shooterpro.app.data.ShooterDataManager;
import com.shooterpro.app.utils.DateUtils;
import com.shooterpro.app.utils.DisplayUtils;
import com.shooterpro.app.ui.ButtonFactory;
import com.shooterpro.app.ui.TextViewFactory;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends BaseActivity {
    
    private ShooterDataManager dataManager;
    private Calendar calendar;
    private int currentYear, currentMonth, currentDay;
    private int selectedYear, selectedMonth;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dataManager = ShooterDataManager.getInstance(this);
        String currentShooter = dataManager.getCurrentShooter();
        
        if (currentShooter.isEmpty()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        
        calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        
        selectedYear = currentYear;
        selectedMonth = currentMonth;
        
        createCalendarScreen();
    }
    
    private void createCalendarScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);
        mainLayout.setPadding(0, DisplayUtils.dpToPx(this, 4), 0, DisplayUtils.dpToPx(this, 4));
        
        // Заголовок
        TextView title = TextViewFactory.createTitle(this, "🗓 " + dataManager.getCurrentShooter());
        mainLayout.addView(title);
        
        // Выбор месяца
        mainLayout.addView(createMonthSelector());
        
        // Дни недели
        mainLayout.addView(createWeekdays());
        
        // Сетка календаря
        mainLayout.addView(createCalendarGrid());
        
        // Легенда
        mainLayout.addView(createLegend());
        
        // Статус сегодня
        addTodayStatus(mainLayout);
        
        // Кнопки действий
        Button trainingBtn = ButtonFactory.createSecondaryButton(this, "🏁 Тренировка");
        trainingBtn.setOnClickListener(v -> openTodayActivity(DayActivity.class));
        mainLayout.addView(trainingBtn);
        
        Button learningBtn = ButtonFactory.createPrimaryButton(this, "📚 Обучение");
        learningBtn.setOnClickListener(v -> openTodayActivity(LearningActivity.class));
        mainLayout.addView(learningBtn);
        
        Button statsBtn = ButtonFactory.createWarningButton(this, "📊 Расширенная статистика");
        statsBtn.setOnClickListener(v -> startActivity(new Intent(this, ExtendedStatsActivity.class)));
        mainLayout.addView(statsBtn);
        
        Button settingsBtn = ButtonFactory.createBackButton(this, "⚙ Настройки");
        settingsBtn.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        mainLayout.addView(settingsBtn);
        
        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
    
    private LinearLayout createMonthSelector() {
        LinearLayout selector = new LinearLayout(this);
        selector.setOrientation(LinearLayout.HORIZONTAL);
        selector.setGravity(Gravity.CENTER);
        selector.setPadding(0, 0, 0, DisplayUtils.dpToPx(this, 16));
        
        Button prevBtn = ButtonFactory.createNavButton(this, "◀");
        prevBtn.setOnClickListener(v -> {
            selectedMonth--;
            if (selectedMonth < 0) {
                selectedMonth = 11;
                selectedYear--;
            }
            createCalendarScreen();
        });
        selector.addView(prevBtn);
        
        String[] months = {"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь",
            "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        
        TextView monthYear = new TextView(this);
        monthYear.setText(months[selectedMonth] + " " + selectedYear);
        monthYear.setTextColor(0xFFE2E8F0);
        monthYear.setTextSize(18);
        monthYear.setPadding(DisplayUtils.dpToPx(this, 16), DisplayUtils.dpToPx(this, 8), 
                           DisplayUtils.dpToPx(this, 16), DisplayUtils.dpToPx(this, 8));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8), 0);
        monthYear.setLayoutParams(params);
        selector.addView(monthYear);
        
        Button nextBtn = ButtonFactory.createNavButton(this, "▶");
        nextBtn.setOnClickListener(v -> {
            selectedMonth++;
            if (selectedMonth > 11) {
                selectedMonth = 0;
                selectedYear++;
            }
            createCalendarScreen();
        });
        selector.addView(nextBtn);
        
        return selector;
    }
    
    private LinearLayout createWeekdays() {
        LinearLayout weekdays = new LinearLayout(this);
        weekdays.setOrientation(LinearLayout.HORIZONTAL);
        
        String[] days = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        int cellSize = DisplayUtils.getScreenWidth(this) / 7;
        
        for (String day : days) {
            TextView tv = new TextView(this);
            tv.setText(day);
            tv.setTextSize(12);
            tv.setGravity(Gravity.CENTER);
            
            if (day.equals("ВС")) {
                tv.setTextColor(0xFFF56565);
            } else if (day.equals("ПТ")) {
                tv.setTextColor(0xFFECC94B);
            } else {
                tv.setTextColor(0xFFA0AEC0);
            }
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                cellSize,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            tv.setLayoutParams(params);
            weekdays.addView(tv);
        }
        
        return weekdays;
    }
    
    private GridLayout createCalendarGrid() {
        GridLayout grid = new GridLayout(this);
        grid.setRowCount(6);
        grid.setColumnCount(7);
        
        calendar.set(selectedYear, selectedMonth, 1);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        int startOffset = (firstDayOfWeek == 1) ? 6 : firstDayOfWeek - 2;
        
        int dayCounter = 1;
        int cellSize = DisplayUtils.getScreenWidth(this) / 7;
        
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                Button button = new Button(this);
                int index = row * 7 + col;
                
                if (index < startOffset || dayCounter > daysInMonth) {
                    button.setVisibility(View.INVISIBLE);
                } else {
                    final int day = dayCounter;
                    button.setText(String.valueOf(day));
                    button.setTextSize(14);
                    button.setOnClickListener(v -> handleDayClick(day));
                    setupDayButton(button, day);
                    dayCounter++;
                }
                
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = cellSize;
                params.height = cellSize;
                params.setMargins(2, 2, 2, 2);
                params.rowSpec = GridLayout.spec(row);
                params.columnSpec = GridLayout.spec(col);
                button.setLayoutParams(params);
                
                grid.addView(button);
            }
        }
        
        return grid;
    }
    
    private void setupDayButton(Button button, int day) {
        String shooter = dataManager.getCurrentShooter();
        String dateKey = DateUtils.formatDate(day, selectedMonth, selectedYear);
        
        boolean hasTraining = dataManager.hasTraining(shooter, dateKey);
        boolean hasTest = dataManager.hasTest(shooter, dateKey);
        boolean isToday = DateUtils.isToday(selectedYear, selectedMonth, day);
        boolean isPast = DateUtils.isPast(selectedYear, selectedMonth, day);
        boolean isSunday = DateUtils.isSunday(selectedYear, selectedMonth, day);
        
        if (isSunday) {
            button.setTextColor(0xFFF56565);
            button.setBackgroundColor(0xFF4A5568);
        } else if (DateUtils.isFriday(selectedYear, selectedMonth, day)) {
            button.setTextColor(0xFFECC94B);
            button.setBackgroundColor(0xFF2A3B5A);
        } else {
            button.setTextColor(0xFFCBD5E0);
            button.setBackgroundColor(0xFF2A3B5A);
        }
        
        if (isToday) {
            if (hasTraining && hasTest) {
                button.setBackgroundColor(0xFF9F7AEA);
            } else if (hasTest) {
                button.setBackgroundColor(0xFFECC94B);
                button.setTextColor(0xFF2D3748);
            } else if (hasTraining) {
                button.setBackgroundColor(0xFF9F7AEA);
            } else {
                button.setBackgroundColor(0xFF4FD1C7);
                button.setTextColor(0xFF2D3748);
            }
        } else if (hasTraining && hasTest) {
            button.setBackgroundColor(0xFF9F7AEA);
        } else if (hasTest) {
            button.setBackgroundColor(0xFFECC94B);
            button.setTextColor(0xFF2D3748);
        } else if (hasTraining) {
            button.setBackgroundColor(0xFF9F7AEA);
        } else if (isPast) {
            button.setBackgroundColor(0xFF1A2B4A);
        }
    }
    
    private LinearLayout createLegend() {
        LinearLayout legend = new LinearLayout(this);
        legend.setOrientation(LinearLayout.HORIZONTAL);
        legend.setGravity(Gravity.CENTER);
        legend.setPadding(0, DisplayUtils.dpToPx(this, 16), 0, DisplayUtils.dpToPx(this, 16));
        
        String[] items = {"■ Сегодня", "■ Тренировка", "■ Зачёт", "■ Выходной"};
        int[] colors = {0xFF4FD1C7, 0xFF9F7AEA, 0xFFECC94B, 0xFFF56565};
        
        for (int i = 0; i < items.length; i++) {
            TextView item = new TextView(this);
            item.setText(items[i]);
            item.setTextColor(colors[i]);
            item.setTextSize(12);
            item.setGravity(Gravity.CENTER);
            
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            if (i < items.length - 1) {
                params.setMargins(0, 0, DisplayUtils.dpToPx(this, 12), 0);
            }
            item.setLayoutParams(params);
            legend.addView(item);
        }
        
        return legend;
    }
    
    private void addTodayStatus(LinearLayout mainLayout) {
        String shooter = dataManager.getCurrentShooter();
        String todayKey = DateUtils.formatDate(currentDay, currentMonth, currentYear);
        
        boolean hasTraining = dataManager.hasTraining(shooter, todayKey);
        boolean hasTest = dataManager.hasTest(shooter, todayKey);
        
        String dayName = DateUtils.getWeekday(currentYear, currentMonth, currentDay);
        String statusMessage = "";
        int statusColor = 0xFF4FD1C7;
        
        if (DateUtils.isSunday(currentYear, currentMonth, currentDay)) {
            statusMessage = "🌟 Сегодня " + dayName + " - Выходной";
            statusColor = 0xFFF56565;
        } else if (DateUtils.isFriday(currentYear, currentMonth, currentDay)) {
            statusMessage = "🎯 Сегодня " + dayName + " - День зачета";
            statusColor = 0xFFECC94B;
        } else {
            statusMessage = "🏁 Сегодня " + dayName + " - День тренировки";
            statusColor = 0xFF9F7AEA;
        }
        
        if (hasTraining || hasTest) {
            if (hasTest) {
                statusMessage = "✅ " + dayName + " - Зачёт пройден";
                statusColor = 0xFFECC94B;
            } else if (hasTraining) {
                statusMessage = "✅ " + dayName + " - Тренировка завершена";
                statusColor = 0xFF9F7AEA;
            }
        }
        
        TextView statusText = new TextView(this);
        statusText.setText(statusMessage);
        statusText.setTextColor(statusColor);
        statusText.setTextSize(16);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, DisplayUtils.dpToPx(this, 20), 0, DisplayUtils.dpToPx(this, 20));
        
        mainLayout.addView(statusText);
    }
    
    private void handleDayClick(int day) {
        String shooter = dataManager.getCurrentShooter();
        String dateKey = DateUtils.formatDate(day, selectedMonth, selectedYear);
        
        boolean hasData = dataManager.hasTraining(shooter, dateKey) || 
                         dataManager.hasTest(shooter, dateKey);
        
        boolean isToday = DateUtils.isToday(selectedYear, selectedMonth, day);
        boolean isPast = DateUtils.isPast(selectedYear, selectedMonth, day);
        
        if (isToday) {
            openDayActivity(day, DayActivity.class);
        } else if (isPast && hasData) {
            openDayActivity(day, StatsActivity.class);
        } else if (isPast) {
            Toast.makeText(this, "Прошедший день без данных", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Будущий день", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void openTodayActivity(Class<?> activityClass) {
        Calendar today = Calendar.getInstance();
        int day = today.get(Calendar.DAY_OF_MONTH);
        int month = today.get(Calendar.MONTH);
        int year = today.get(Calendar.YEAR);
        
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("day", day);
        intent.putExtra("month", month);
        intent.putExtra("year", year);
        startActivity(intent);
    }
    
    private void openDayActivity(int day, Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        intent.putExtra("day", day);
        intent.putExtra("month", selectedMonth);
        intent.putExtra("year", selectedYear);
        startActivity(intent);
    }
}
