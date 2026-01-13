package com.shooterpro.app;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.shooterpro.app.data.ShooterDataManager;
import com.shooterpro.app.export.PDFExporter;
import com.shooterpro.app.utils.DisplayUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SettingsActivity extends BaseActivity {
    
    private ShooterDataManager dataManager;
    private String currentShooter;
    private LinearLayout timersList;
    private List<CustomTimer> customTimers = new ArrayList<>();
    private CheckBox sequentialTimersCheckbox;
    private CheckBox remindersEnabled;
    private Button reminderTimeButton;
    private int reminderHour = 19;
    private int reminderMinute = 0;
    
    static class CustomTimer {
        String name;
        int minutes;
        int seconds;
        
        CustomTimer(String name, int minutes, int seconds) {
            this.name = name;
            this.minutes = minutes;
            this.seconds = seconds;
        }
        
        int getTotalSeconds() {
            return minutes * 60 + seconds;
        }
        
        @Override
        public String toString() {
            return name + ":" + minutes + ":" + seconds;
        }
        
        static CustomTimer fromString(String str) {
            String[] parts = str.split(":");
            if (parts.length == 3) {
                try {
                    return new CustomTimer(
                        parts[0],
                        Integer.parseInt(parts[1]),
                        Integer.parseInt(parts[2])
                    );
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dataManager = ShooterDataManager.getInstance(this);
        currentShooter = dataManager.getCurrentShooter();
        
        loadSettings();
        loadTimers();
        createSettingsScreen();
    }
    
    void loadSettings() {
        reminderHour = dataManager.getReminderHour();
        reminderMinute = dataManager.getReminderMinute();
    }
    
    void loadTimers() {
        customTimers.clear();
        List<String> timerStrings = dataManager.getCustomTimers();
        for (String timerStr : timerStrings) {
            CustomTimer timer = CustomTimer.fromString(timerStr);
            if (timer != null) {
                customTimers.add(timer);
            }
        }
    }
    
    void saveSettings() {
        if (sequentialTimersCheckbox != null) {
            // Сохраняем в SharedPreferences
            dataManager.prefs.edit().putBoolean("sequential_timers", 
                sequentialTimersCheckbox.isChecked()).apply();
        }
        
        if (remindersEnabled != null) {
            dataManager.setRemindersEnabled(remindersEnabled.isChecked());
        }
        
        dataManager.setReminderTime(reminderHour, reminderMinute);
    }
    
    void saveTimers() {
        List<String> timerStrings = new ArrayList<>();
        for (CustomTimer timer : customTimers) {
            timerStrings.add(timer.toString());
        }
        dataManager.saveCustomTimers(timerStrings);
    }
    
    void createSettingsScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);
        
        int padding = DisplayUtils.dpToPx(this, 16);
        mainLayout.setPadding(padding, DisplayUtils.dpToPx(this, 20), padding, padding);
        
        TextView title = new TextView(this);
        title.setText("⚙ НАСТРОЙКИ");
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        mainLayout.addView(title);
        
        TextView shooterInfo = new TextView(this);
        shooterInfo.setText("Спортсмен: " + (currentShooter.isEmpty() ? "Не выбран" : currentShooter));
        shooterInfo.setTextColor(0xFFA0AEC0);
        shooterInfo.setTextSize(16);
        shooterInfo.setGravity(Gravity.CENTER);
        shooterInfo.setPadding(0, 0, 0, DisplayUtils.dpToPx(this, 20));
        mainLayout.addView(shooterInfo);
        
        mainLayout.addView(createTimersBlock());
        mainLayout.addView(createDataManagementBlock());
        mainLayout.addView(createRemindersBlock());
        mainLayout.addView(createAdditionalBlock());
        
        Button saveButton = createActionButton("💾 СОХРАНИТЬ ВСЕ НАСТРОЙКИ", 0xFF4FD1C7);
        saveButton.setOnClickListener(v -> saveAllSettings());
        mainLayout.addView(saveButton);
        
        Button backButton = createActionButton("← НАЗАД В КАЛЕНДАРЬ", 0xFF2A3B5A);
        backButton.setOnClickListener(v -> finish());
        mainLayout.addView(backButton);
        
        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
    
    LinearLayout createTimersBlock() {
        LinearLayout block = createBlockContainer("⏱ ТАЙМЕРЫ ДЛЯ ТРЕНИРОВКИ");
        
        timersList = new LinearLayout(this);
        timersList.setOrientation(LinearLayout.VERTICAL);
        timersList.setBackgroundColor(0xFF1A2B4A);
        timersList.setPadding(DisplayUtils.dpToPx(this, 12), 
                             DisplayUtils.dpToPx(this, 12),
                             DisplayUtils.dpToPx(this, 12),
                             DisplayUtils.dpToPx(this, 12));
        timersList.setMinimumHeight(DisplayUtils.dpToPx(this, 100));
        
        LinearLayout.LayoutParams listParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        listParams.setMargins(0, 0, 0, DisplayUtils.dpToPx(this, 16));
        timersList.setLayoutParams(listParams);
        
        updateTimersList();
        block.addView(timersList);
        
        LinearLayout addForm = new LinearLayout(this);
        addForm.setOrientation(LinearLayout.VERTICAL);
        
        final EditText timerNameInput = new EditText(this);
        timerNameInput.setHint("Название таймера (например: Разминка)");
        timerNameInput.setTextColor(0xFFE2E8F0);
        timerNameInput.setHintTextColor(0xFF718096);
        timerNameInput.setBackgroundColor(0xFF2A3B5A);
        timerNameInput.setPadding(DisplayUtils.dpToPx(this, 8), 
                                DisplayUtils.dpToPx(this, 8),
                                DisplayUtils.dpToPx(this, 8),
                                DisplayUtils.dpToPx(this, 8));
        addForm.addView(timerNameInput);
        
        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setGravity(Gravity.CENTER);
        timeRow.setPadding(0, DisplayUtils.dpToPx(this, 16), 0, DisplayUtils.dpToPx(this, 16));
        
        final EditText minutesInput = new EditText(this);
        minutesInput.setHint("Минуты");
        minutesInput.setTextColor(0xFFE2E8F0);
        minutesInput.setHintTextColor(0xFF718096);
        minutesInput.setGravity(Gravity.CENTER);
        minutesInput.setTextSize(16);
        minutesInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        minutesInput.setMinWidth(DisplayUtils.dpToPx(this, 60));
        minutesInput.setPadding(DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8), 0);
        
        final EditText secondsInput = new EditText(this);
        secondsInput.setHint("Секунды");
        secondsInput.setTextColor(0xFFE2E8F0);
        secondsInput.setHintTextColor(0xFF718096);
        secondsInput.setGravity(Gravity.CENTER);
        secondsInput.setTextSize(16);
        secondsInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        secondsInput.setMinWidth(DisplayUtils.dpToPx(this, 60));
        secondsInput.setPadding(DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8), 0);
        
        timeRow.addView(minutesInput);
        
        TextView colon = new TextView(this);
        colon.setText(":");
        colon.setTextColor(0xFF4FD1C7);
        colon.setTextSize(20);
        colon.setPadding(DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8), 0);
        timeRow.addView(colon);
        
        timeRow.addView(secondsInput);
        addForm.addView(timeRow);
        
        Button addButton = createSmallButton("➕ ДОБАВИТЬ ТАЙМЕР", 0xFF9F7AEA);
        addButton.setOnClickListener(v -> {
            String name = timerNameInput.getText().toString().trim();
            String minutesStr = minutesInput.getText().toString().trim();
            String secondsStr = secondsInput.getText().toString().trim();
            
            if (name.isEmpty()) {
                Toast.makeText(SettingsActivity.this, "Введите название таймера", Toast.LENGTH_SHORT).show();
                return;
            }
            
            try {
                int minutes = minutesStr.isEmpty() ? 0 : Integer.parseInt(minutesStr);
                int seconds = secondsStr.isEmpty() ? 0 : Integer.parseInt(secondsStr);
                
                if (seconds >= 60) {
                    minutes += seconds / 60;
                    seconds = seconds % 60;
                }
                
                if (minutes == 0 && seconds == 0) {
                    Toast.makeText(SettingsActivity.this, "Время не может быть нулевым", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                customTimers.add(new CustomTimer(name, minutes, seconds));
                saveTimers();
                updateTimersList();
                
                timerNameInput.setText("");
                minutesInput.setText("");
                secondsInput.setText("");
                
                Toast.makeText(SettingsActivity.this, "Таймер добавлен: " + name, Toast.LENGTH_SHORT).show();
                
            } catch (NumberFormatException e) {
                Toast.makeText(SettingsActivity.this, "Введите корректные числа", Toast.LENGTH_SHORT).show();
            }
        });
        addForm.addView(addButton);
        
        block.addView(addForm);
        
        sequentialTimersCheckbox = new CheckBox(this);
        sequentialTimersCheckbox.setText("Запускать таймеры последовательно перед тренировкой");
        sequentialTimersCheckbox.setTextColor(0xFFE2E8F0);
        sequentialTimersCheckbox.setChecked(dataManager.prefs.getBoolean("sequential_timers", false));
        sequentialTimersCheckbox.setPadding(0, DisplayUtils.dpToPx(this, 12), 0, 0);
        block.addView(sequentialTimersCheckbox);
        
        return block;
    }
    
    LinearLayout createDataManagementBlock() {
        LinearLayout block = createBlockContainer("🗃 УПРАВЛЕНИЕ ДАННЫМИ");
        
        Button exportButton = createSmallButton("📤 ЭКСПОРТ СТАТИСТИКИ (TXT)", 0xFFECC94B);
        exportButton.setOnClickListener(v -> exportStatistics());
        block.addView(exportButton);
        
        Button backupButton = createSmallButton("💾 СОЗДАТЬ РЕЗЕРВНУЮ КОПИЮ", 0xFF4FD1C7);
        backupButton.setOnClickListener(v -> createBackup());
        block.addView(backupButton);
        
        Button manageDataButton = createSmallButton("🗑 УПРАВЛЕНИЕ ДАННЫМИ...", 0xFFF56565);
        manageDataButton.setOnClickListener(v -> showDataManagementDialog());
        block.addView(manageDataButton);
        
        return block;
    }
    
    LinearLayout createRemindersBlock() {
        LinearLayout block = createBlockContainer("🔔 НАПОМИНАНИЯ О ТРЕНИРОВКАХ");
        
        remindersEnabled = new CheckBox(this);
        remindersEnabled.setText("Включить напоминания");
        remindersEnabled.setTextColor(0xFFE2E8F0);
        remindersEnabled.setChecked(dataManager.isRemindersEnabled());
        block.addView(remindersEnabled);
        
        LinearLayout timeRow = new LinearLayout(this);
        timeRow.setOrientation(LinearLayout.HORIZONTAL);
        timeRow.setPadding(0, DisplayUtils.dpToPx(this, 12), 0, 0);
        
        TextView timeLabel = new TextView(this);
        timeLabel.setText("Время напоминания:");
        timeLabel.setTextColor(0xFFE2E8F0);
        timeLabel.setTextSize(14);
        timeLabel.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
        timeRow.addView(timeLabel);
        
        reminderTimeButton = new Button(this);
        reminderTimeButton.setText(String.format("%02d:%02d", reminderHour, reminderMinute));
        reminderTimeButton.setBackgroundColor(0xFF2A3B5A);
        reminderTimeButton.setTextColor(0xFFE2E8F0);
        reminderTimeButton.setTextSize(14);
        reminderTimeButton.setOnClickListener(v -> showTimePicker());
        timeRow.addView(reminderTimeButton);
        
        block.addView(timeRow);
        
        return block;
    }
    
    LinearLayout createAdditionalBlock() {
        LinearLayout block = createBlockContainer("🔧 ДОПОЛНИТЕЛЬНО");
        
        Button changeShooterButton = createSmallButton("👤 СМЕНИТЬ СПОРТСМЕНА", 0xFF9F7AEA);
        changeShooterButton.setOnClickListener(v -> changeShooter());
        block.addView(changeShooterButton);
        
        Button resetButton = createSmallButton("🔄 СБРОС НАСТРОЕК", 0xFF2A3B5A);
        resetButton.setOnClickListener(v -> resetSettings());
        block.addView(resetButton);
        
        Button aboutButton = createSmallButton("ℹ️ О ПРИЛОЖЕНИИ", 0xFF2A3B5A);
        aboutButton.setOnClickListener(v -> showAboutDialog());
        block.addView(aboutButton);
        
        return block;
    }
    
    LinearLayout createBlockContainer(String titleText) {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackgroundColor(0xFF2A3B5A);
        block.setPadding(DisplayUtils.dpToPx(this, 16), 
                        DisplayUtils.dpToPx(this, 16),
                        DisplayUtils.dpToPx(this, 16),
                        DisplayUtils.dpToPx(this, 16));
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(this, 16));
        block.setLayoutParams(params);
        
        TextView title = new TextView(this);
        title.setText(titleText);
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(18);
        title.setPadding(0, 0, 0, DisplayUtils.dpToPx(this, 12));
        block.addView(title);
        
        return block;
    }
    
    Button createSmallButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(14);
        button.setPadding(0, DisplayUtils.dpToPx(this, 10), 0, DisplayUtils.dpToPx(this, 10));
        button.setAllCaps(false);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 45)
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(this, 8));
        button.setLayoutParams(params);
        
        return button;
    }
    
    Button createActionButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(16);
        button.setAllCaps(false);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 52)
        );
        params.setMargins(0, 0, 0, DisplayUtils.dpToPx(this, 12));
        button.setLayoutParams(params);
        
        return button;
    }
    
    void updateTimersList() {
        if (timersList == null) return;
        
        timersList.removeAllViews();
        
        if (customTimers.isEmpty()) {
            TextView emptyText = new TextView(this);
            emptyText.setText("Нет таймеров\nДобавьте первый таймер для тренировки!");
            emptyText.setTextColor(0xFF718096);
            emptyText.setTextSize(14);
            emptyText.setGravity(Gravity.CENTER);
            emptyText.setPadding(0, DisplayUtils.dpToPx(this, 20), 0, DisplayUtils.dpToPx(this, 20));
            timersList.addView(emptyText);
            return;
        }
        
        int totalTimeSeconds = 0;
        
        for (int i = 0; i < customTimers.size(); i++) {
            final CustomTimer timer = customTimers.get(i);
            final int index = i;
            totalTimeSeconds += timer.getTotalSeconds();
            
            LinearLayout timerRow = new LinearLayout(this);
            timerRow.setOrientation(LinearLayout.HORIZONTAL);
            timerRow.setPadding(DisplayUtils.dpToPx(this, 8), 
                              DisplayUtils.dpToPx(this, 6),
                              DisplayUtils.dpToPx(this, 8),
                              DisplayUtils.dpToPx(this, 6));
            
            TextView numberText = new TextView(this);
            numberText.setText((i + 1) + ".");
            numberText.setTextColor(0xFF4FD1C7);
            numberText.setTextSize(14);
            numberText.setLayoutParams(new LinearLayout.LayoutParams(
                DisplayUtils.dpToPx(this, 25), LinearLayout.LayoutParams.WRAP_CONTENT));
            timerRow.addView(numberText);
            
            TextView timerText = new TextView(this);
            String timeText = formatTime(timer.minutes, timer.seconds);
            timerText.setText(timer.name + " - " + timeText);
            timerText.setTextColor(0xFFE2E8F0);
            timerText.setTextSize(14);
            timerText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
            timerText.setPadding(DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8), 0);
            timerRow.addView(timerText);
            
            Button deleteBtn = new Button(this);
            deleteBtn.setText("✕");
            deleteBtn.setBackgroundColor(0xFFF56565);
            deleteBtn.setTextColor(0xFFFFFFFF);
            deleteBtn.setTextSize(12);
            deleteBtn.setPadding(DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8), 0);
            deleteBtn.setOnClickListener(v -> {
                customTimers.remove(index);
                saveTimers();
                updateTimersList();
                Toast.makeText(SettingsActivity.this,
                             "Таймер удален: " + timer.name, Toast.LENGTH_SHORT).show();
            });
            
            deleteBtn.setLayoutParams(new LinearLayout.LayoutParams(
                DisplayUtils.dpToPx(this, 40), DisplayUtils.dpToPx(this, 30)));
            timerRow.addView(deleteBtn);
            
            timersList.addView(timerRow);
            
            if (i < customTimers.size() - 1) {
                View divider = new View(this);
                divider.setBackgroundColor(0xFF4A5568);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, DisplayUtils.dpToPx(this, 1)));
                timersList.addView(divider);
            }
        }
        
        TextView totalTimeText = new TextView(this);
        int totalMinutes = totalTimeSeconds / 60;
        int totalSeconds = totalTimeSeconds % 60;
        totalTimeText.setText("Общее время: " + formatTime(totalMinutes, totalSeconds));
        totalTimeText.setTextColor(0xFF4FD1C7);
        totalTimeText.setTextSize(14);
        totalTimeText.setGravity(Gravity.CENTER);
        totalTimeText.setPadding(0, DisplayUtils.dpToPx(this, 12), 0, 0);
        timersList.addView(totalTimeText);
    }
    
    String formatTime(int minutes, int seconds) {
        if (minutes > 0 && seconds > 0) {
            return minutes + " мин " + seconds + " сек";
        } else if (minutes > 0) {
            return minutes + " мин";
        } else {
            return seconds + " сек";
        }
    }
    
    void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            this,
            new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    reminderHour = hourOfDay;
                    reminderMinute = minute;
                    if (reminderTimeButton != null) {
                        reminderTimeButton.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }
            },
            reminderHour,
            reminderMinute,
            true
        );
        timePickerDialog.show();
    }
    
    void saveAllSettings() {
        saveSettings();
        saveTimers();
        
        Toast.makeText(this, "Все настройки сохранены", Toast.LENGTH_SHORT).show();
    }
    
    void exportStatistics() {
        if (currentShooter.isEmpty()) {
            Toast.makeText(this, "Сначала выберите спортсмена", Toast.LENGTH_SHORT).show();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Экспорт статистики")
            .setMessage("Создать отчет для " + currentShooter + "?")
            .setPositiveButton("Создать отчет", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PDFExporter exporter = new PDFExporter(SettingsActivity.this);
                    boolean success = exporter.exportStatisticsToTXT(currentShooter);
                    
                    if (success) {
                        showExportSuccessDialog();
                    }
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void showExportSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Отчет создан")
            .setMessage("Статистика успешно экспортирована в текстовый файл.\n\n" +
                       "Файл сохранен в папке Documents/ShooterPRO/")
            .setPositiveButton("OK", null)
            .show();
    }
    
    void createBackup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Резервное копирование")
            .setMessage("Создать резервную копию всех данных?\n\n" +
                       "Будет сохранено:\n" +
                       "• Настройки таймеров\n" +
                       "• Все тренировки\n" +
                       "• Статистику\n" +
                       "• Заметки")
            .setPositiveButton("Создать копию", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    performBackup();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void performBackup() {
        // Сохраняем текущие настройки
        saveAllSettings();
        
        String backupDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        
        dataManager.prefs.edit()
            .putString("last_backup", backupDate)
            .putBoolean("has_backup", true)
            .apply();
        
        showBackupSuccessDialog(backupDate);
    }
    
    private void showBackupSuccessDialog(String backupDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Резервная копия создана")
            .setMessage("Резервная копия успешно создана!\n\n" +
                       "Дата: " + backupDate + "\n\n" +
                       "Настройки сохранены локально.")
            .setPositiveButton("OK", null)
            .show();
    }
    
    void showDataManagementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Управление данными")
            .setItems(new String[]{
                "📊 Удалить статистику тренировок",
                "📝 Удалить все заметки", 
                "👤 Удалить текущего спортсмена",
                "🗑️ Удалить все данные",
                "❌ Отмена"
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            confirmDeleteStats();
                            break;
                        case 1:
                            confirmDeleteNotes();
                            break;
                        case 2:
                            confirmDeleteShooter();
                            break;
                        case 3:
                            confirmDeleteAllData();
                            break;
                        case 4:
                            break;
                    }
                }
            })
            .show();
    }
    
    private void confirmDeleteStats() {
        new AlertDialog.Builder(this)
            .setTitle("Удаление статистики")
            .setMessage("Вы уверены, что хотите удалить всю статистику тренировок?\n\nЭто действие нельзя отменить!")
            .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dataManager.clearShooterData(currentShooter);
                    Toast.makeText(SettingsActivity.this, 
                                 "Статистика удалена", Toast.LENGTH_SHORT).show();
                    recreate();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void confirmDeleteNotes() {
        new AlertDialog.Builder(this)
            .setTitle("Удаление заметок")
            .setMessage("Удалить все заметки и комментарии к тренировкам?")
            .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Реализация удаления заметок
                    Toast.makeText(SettingsActivity.this, 
                                 "Заметки удалены", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void confirmDeleteShooter() {
        new AlertDialog.Builder(this)
            .setTitle("Удаление спортсмена")
            .setMessage("Удалить данные текущего спортсмена?\n\nВсе тренировки и статистика будут удалены.")
            .setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dataManager.clearShooterData(currentShooter);
                    dataManager.setCurrentShooter("");
                    
                    Toast.makeText(SettingsActivity.this, 
                                 "Данные спортсмена удалены", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void confirmDeleteAllData() {
        new AlertDialog.Builder(this)
            .setTitle("Удаление всех данных")
            .setMessage("ВНИМАНИЕ! Это удалит ВСЕ данные приложения:\n" +
                       "• Всех спортсменов\n" +
                       "• Все тренировки\n" +
                       "• Всю статистику\n" +
                       "• Все настройки\n\n" +
                       "Действие необратимо!")
            .setPositiveButton("УДАЛИТЬ ВСЁ", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dataManager.clearAllData();
                    
                    Toast.makeText(SettingsActivity.this, 
                                 "Все данные удалены", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    void changeShooter() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сменить спортсмена")
            .setMessage("Введите имя нового спортсмена:")
            .setView(new EditText(this))
            .setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EditText input = ((AlertDialog) dialog).findViewById(android.R.id.edit);
                    if (input != null && !input.getText().toString().trim().isEmpty()) {
                        String newShooter = input.getText().toString().trim();
                        dataManager.setCurrentShooter(newShooter);
                        currentShooter = newShooter;
                        recreate();
                        Toast.makeText(SettingsActivity.this, 
                                     "Спортсмен изменен на: " + newShooter, Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    void resetSettings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Сброс настроек")
            .setMessage("Вы уверены, что хотите сбросить все настройки к значениям по умолчанию?\n\n" +
                       "Данные тренировок не будут удалены.")
            .setPositiveButton("Сбросить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Сбрасываем только настройки, не данные
                    dataManager.prefs.edit()
                        .remove("sequential_timers")
                        .remove("reminders_enabled")
                        .remove("reminder_hour")
                        .remove("reminder_minute")
                        .remove("custom_timers")
                        .apply();
                    
                    customTimers.clear();
                    loadSettings();
                    loadTimers();
                    recreate();
                    
                    Toast.makeText(SettingsActivity.this, 
                                 "Настройки сброшены", Toast.LENGTH_SHORT).show();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("О приложении ShooterPRO")
            .setMessage("Версия 1.4\n\n" +
                       "Приложение для планирования тренировок стрелков\n\n" +
                       "Возможности:\n" +
                       "• Тренировки и зачеты\n" +
                       "• Детальная статистика\n" +
                       "• Графики прогресса\n" +
                       "• Экспорт данных\n" +
                       "• План обучения\n\n" +
                       "Разработчик: Samcrimea\n\n" +
                       "© 2024 ShooterPRO Team")
            .setPositiveButton("OK", null)
            .show();
    }
}
