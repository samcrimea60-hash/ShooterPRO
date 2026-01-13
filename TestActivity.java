package com.shooterpro.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TestActivity extends BaseActivity {

    private SharedPreferences prefs;
    private String currentShooter, dateKey;
    private ArrayList<Float> shotsList = new ArrayList<>();
    private long testStartTime, timeRemaining = 75 * 60;
    private Handler timerHandler = new Handler();
    private Runnable timerRunnable;
    private boolean isTimerRunning = false;
    private TextView timerView, countdownView, totalShotsView, totalScoreView;
    private TextView[] seriesViews = new TextView[6];
    private EditText inputScore;
    private DecimalFormat df = new DecimalFormat("0.0");
    private int currentSeries = 0;
    private int shotsInCurrentSeries = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("ShooterPRO", MODE_PRIVATE);
        currentShooter = prefs.getString("current_shooter", "");

        int day = getIntent().getIntExtra("day", 1);
        int month = getIntent().getIntExtra("month", 0);
        int year = getIntent().getIntExtra("year", 2024);
        dateKey = String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year);

        testStartTime = System.currentTimeMillis();

        createTestScreen();
        startTestTimer();
    }

    private void createTestScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);

        int padding = getAdaptivePadding();
        mainLayout.setPadding(padding, dpToPx(20), padding, padding);

        TextView title = new TextView(this);
        title.setText("🎯 ЗАЧЕТНАЯ СТРЕЛЬБА");
        title.setTextColor(0xFFECC94B);
        title.setTextSize(getTextSize(18));
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mainLayout.addView(title);

        timerView = new TextView(this);
        timerView.setText("00:00");
        timerView.setTextColor(0xFFE2E8F0);
        timerView.setTextSize(getTextSize(16));
        timerView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        mainLayout.addView(timerView);

        countdownView = new TextView(this);
        countdownView.setText(formatTime(timeRemaining));
        countdownView.setTextColor(0xFFECC94B);
        countdownView.setTextSize(getTextSize(22));
        countdownView.setGravity(View.TEXT_ALIGNMENT_CENTER);
        countdownView.setPadding(0, 0, 0, dpToPx(20));
        mainLayout.addView(countdownView);

        LinearLayout inputBlock = createInputBlock();
        mainLayout.addView(inputBlock);

        LinearLayout seriesBlock = createSeriesBlock();
        mainLayout.addView(seriesBlock);

        LinearLayout statsBlock = createStatsBlock();
        mainLayout.addView(statsBlock);

        LinearLayout buttonsBlock = createButtonsBlock();
        mainLayout.addView(buttonsBlock);

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        updateDisplay();
    }

    private LinearLayout createInputBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackgroundColor(0xFF2A3B5A);
        block.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        TextView title = new TextView(this);
        title.setText("ВВОД РЕЗУЛЬТАТА (макс 60 выстрелов)");
        title.setTextColor(0xFFE2E8F0);
        title.setTextSize(getTextSize(14));
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        block.addView(title);

        inputScore = new EditText(this);
        inputScore.setText("10.9");
        inputScore.setTextColor(0xFF4FD1C7);
        inputScore.setTextSize(getTextSize(22));
        inputScore.setGravity(View.TEXT_ALIGNMENT_CENTER);
        inputScore.setBackgroundColor(0xFF1A2B4A);
        inputScore.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        inputScore.setSingleLine(true);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dpToPx(50)
        );
        params.setMargins(0, dpToPx(8), 0, dpToPx(8));
        inputScore.setLayoutParams(params);
        block.addView(inputScore);

        TextView hint = new TextView(this);
        hint.setText("Введите от 0.0 до 10.9");
        hint.setTextColor(0xFFA0AEC0);
        hint.setTextSize(getTextSize(12));
        hint.setGravity(View.TEXT_ALIGNMENT_CENTER);
        block.addView(hint);

        Button addBtn = new Button(this);
        addBtn.setText("ДОБАВИТЬ ВЫСТРЕЛ");
        addBtn.setBackgroundColor(0xFF4FD1C7);
        addBtn.setTextColor(0xFFFFFFFF);
        addBtn.setTextSize(getTextSize(14));
        addBtn.setPadding(0, dpToPx(12), 0, dpToPx(12));
        addBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					addShot();
				}
			});

        params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight()
        );
        params.setMargins(0, dpToPx(8), 0, 0);
        addBtn.setLayoutParams(params);
        block.addView(addBtn);

        return block;
    }

    private LinearLayout createSeriesBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackgroundColor(0xFF2A3B5A);
        block.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        TextView title = new TextView(this);
        title.setText("СЕРИИ (по 10 выстрелов, всего 6 серий)");
        title.setTextColor(0xFFE2E8F0);
        title.setTextSize(getTextSize(14));
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        block.addView(title);

        for (int i = 0; i < 6; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);

            TextView label = new TextView(this);
            label.setText("Серия " + (i + 1) + ":");
            label.setTextColor(0xFFA0AEC0);
            label.setTextSize(getTextSize(12));
            label.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
            row.addView(label);

            seriesViews[i] = new TextView(this);
            seriesViews[i].setText("0.0");
            seriesViews[i].setTextColor(0xFFE2E8F0);
            seriesViews[i].setTextSize(getTextSize(12));
            seriesViews[i].setGravity(View.TEXT_ALIGNMENT_TEXT_END);
            seriesViews[i].setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                         LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
            row.addView(seriesViews[i]);

            block.addView(row);
        }

        LinearLayout totalRow = new LinearLayout(this);
        totalRow.setOrientation(LinearLayout.HORIZONTAL);
        totalRow.setBackgroundColor(0xFF1A2B4A);
        totalRow.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        TextView totalLabel = new TextView(this);
        totalLabel.setText("ОБЩИЙ РЕЗУЛЬТАТ:");
        totalLabel.setTextColor(0xFF4FD1C7);
        totalLabel.setTextSize(getTextSize(14));
        totalLabel.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                 LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
        totalRow.addView(totalLabel);

        totalScoreView = new TextView(this);
        totalScoreView.setText("0.0");
        totalScoreView.setTextColor(0xFF4FD1C7);
        totalScoreView.setTextSize(getTextSize(16));
        totalScoreView.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
        totalScoreView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                     LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
        totalRow.addView(totalScoreView);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(12), 0, 0);
        totalRow.setLayoutParams(params);
        block.addView(totalRow);

        return block;
    }

    private LinearLayout createStatsBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.VERTICAL);
        block.setBackgroundColor(0xFF2A3B5A);
        block.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));

        TextView title = new TextView(this);
        title.setText("СТАТИСТИКА");
        title.setTextColor(0xFFE2E8F0);
        title.setTextSize(getTextSize(14));
        title.setGravity(View.TEXT_ALIGNMENT_CENTER);
        block.addView(title);

        LinearLayout shotsRow = createStatRow("Выстрелов:", "0");
        totalShotsView = (TextView) shotsRow.getChildAt(1);
        block.addView(shotsRow);

        block.addView(createStatRow("Средний балл:", "0.0"));
        block.addView(createStatRow("Лучший выстрел:", "0.0"));
        block.addView(createStatRow("Худший выстрел:", "0.0"));
        block.addView(createStatRow("Отрывов (<9.0):", "0"));

        return block;
    }

    private LinearLayout createStatRow(String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFFA0AEC0);
        labelView.setTextSize(getTextSize(12));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.7f));
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFE2E8F0);
        valueView.setTextSize(getTextSize(12));
        valueView.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.3f));
        row.addView(valueView);

        return row;
    }

    private LinearLayout createButtonsBlock() {
        LinearLayout block = new LinearLayout(this);
        block.setOrientation(LinearLayout.HORIZONTAL);
        block.setPadding(0, dpToPx(20), 0, 0);

        Button deleteBtn = createActionButton("УДАЛИТЬ", 0xFF4A5568);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					deleteLastShot();
				}
			});
        block.addView(deleteBtn);

        Button finishBtn = createActionButton("ЗАВЕРШИТЬ", 0xFF4FD1C7);
        finishBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finishTest();
				}
			});
        block.addView(finishBtn);

        Button menuBtn = createActionButton("МЕНЮ", 0xFF1A2B4A);
        menuBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					goToMenu();
				}
			});
        block.addView(menuBtn);

        return block;
    }

    private Button createActionButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(getTextSize(12));
        button.setPadding(0, dpToPx(8), 0, dpToPx(8));
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                                                                         getButtonHeight(), 1);
        params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
        button.setLayoutParams(params);

        return button;
    }

    private void startTestTimer() {
        isTimerRunning = true;

        final Handler trainingTimerHandler = new Handler();
        trainingTimerHandler.post(new Runnable() {
				@Override
				public void run() {
					long elapsed = (System.currentTimeMillis() - testStartTime) / 1000;
					long minutes = elapsed / 60;
					long seconds = elapsed % 60;
					timerView.setText(String.format("%02d:%02d", minutes, seconds));
					trainingTimerHandler.postDelayed(this, 1000);
				}
			});

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (isTimerRunning && timeRemaining > 0) {
                    timeRemaining--;
                    countdownView.setText(formatTime(timeRemaining));

                    if (timeRemaining < 300) {
                        countdownView.setTextColor(0xFFF56565);
                    } else if (timeRemaining < 600) {
                        countdownView.setTextColor(0xFFECC94B);
                    }

                    timerHandler.postDelayed(this, 1000);

                    if (timeRemaining == 0) {
                        countdownView.setText("ВРЕМЯ ВЫШЛО!");
                        countdownView.setTextColor(0xFFF56565);
                        Toast.makeText(TestActivity.this, "Время зачета вышло!",
                                       Toast.LENGTH_SHORT).show();
                        finishTest();
                    }
                }
            }
        };
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private void addShot() {
        if (shotsList.size() >= 60) {
            Toast.makeText(this, "Максимум 60 выстрелов в зачете!",
                           Toast.LENGTH_SHORT).show();
            return;
        }

        String input = inputScore.getText().toString().trim().replace(',', '.');
        if (input.isEmpty()) {
            Toast.makeText(this, "Введите результат!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float score = Float.parseFloat(input);
            if (score < 0.0f || score > 10.9f) {
                Toast.makeText(this, "Введите от 0.0 до 10.9",
                               Toast.LENGTH_SHORT).show();
                return;
            }

            score = Math.round(score * 10) / 10.0f;
            shotsList.add(score);

            // Обновляем текущую серию СНАЧАЛА (включая текущий выстрел)
            updateCurrentSeries();

            // Увеличиваем счетчик выстрелов в серии ПОСЛЕ обновления
            shotsInCurrentSeries++;

            // Если серия заполнена (10 выстрелов), переходим к следующей
            if (shotsInCurrentSeries >= 10) {
                shotsInCurrentSeries = 0;
                currentSeries++;
            }

            updateDisplay();

            Toast.makeText(this, "Выстрел " + shotsList.size() + ": " + df.format(score),
                           Toast.LENGTH_SHORT).show();

            inputScore.setText("");
            inputScore.requestFocus();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ошибка! Введите число (например: 9.5)",
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentSeries() {
        if (currentSeries >= 6) return;

        TextView currentSeriesView = seriesViews[currentSeries];
        float seriesSum = 0;
        int startIndex = currentSeries * 10;

        // Берем все выстрелы с начала текущей серии до текущего момента
        int endIndex = shotsList.size();

        for (int i = startIndex; i < endIndex; i++) {
            seriesSum += shotsList.get(i);
        }

        currentSeriesView.setText(df.format(seriesSum));
    }

    private void deleteLastShot() {
        if (!shotsList.isEmpty()) {
            shotsList.remove(shotsList.size() - 1);

            // Обновляем счетчик выстрелов в серии
            if (shotsInCurrentSeries > 0) {
                shotsInCurrentSeries--;
            } else {
                // Если удалили первый выстрел из серии, переходим к предыдущей
                if (currentSeries > 0) {
                    currentSeries--;
                    shotsInCurrentSeries = 9; // В предыдущей серии было 10 выстрелов
                }
            }

            updateCurrentSeries();
            updateDisplay();
            Toast.makeText(this, "Последний выстрел удален",
                           Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Нет выстрелов для удаления",
                           Toast.LENGTH_SHORT).show();
        }
    }

    private void finishTest() {
        isTimerRunning = false;

        if (shotsList.isEmpty()) {
            Toast.makeText(this, "Зачет отменен", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        saveTestData();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(currentShooter + "_" + dateKey + "_has_test", true);
        editor.putBoolean(currentShooter + "_" + dateKey + "_test_completed", true);

        String todayKey = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date());
        editor.putBoolean(currentShooter + "_" + todayKey + "_test_completed", true);
        editor.apply();

        Toast.makeText(this, "Зачет завершен! Результаты сохранены.",
                       Toast.LENGTH_SHORT).show();
        finish();
    }

    private void goToMenu() {
        finish();
    }

    private void updateDisplay() {
        totalShotsView.setText(String.valueOf(shotsList.size()));

        // Обновляем общий результат
        float totalScore = 0;
        for (int i = 0; i < 6; i++) {
            float seriesSum = 0;
            int startIdx = i * 10;
            int endIdx = Math.min(startIdx + 10, shotsList.size());

            for (int j = startIdx; j < endIdx; j++) {
                seriesSum += shotsList.get(j);
            }

            totalScore += seriesSum;
        }

        totalScoreView.setText(df.format(totalScore));

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
        try {
            ScrollView scrollView = (ScrollView) getWindow().getDecorView()
                .findViewById(android.R.id.content);
            LinearLayout mainLayout = (LinearLayout) scrollView.getChildAt(0);
            LinearLayout statsBlock = (LinearLayout) mainLayout.getChildAt(3);

            if (statsBlock != null && statsBlock.getChildCount() > index) {
                LinearLayout row = (LinearLayout) statsBlock.getChildAt(index);
                if (row != null && row.getChildCount() > 1) {
                    TextView valueView = (TextView) row.getChildAt(1);
                    valueView.setText(value);
                }
            }
        } catch (Exception e) {
        }
    }

    private void saveTestData() {
        try {
            JSONObject testData = new JSONObject();
            JSONArray shotsArray = new JSONArray();
            for (float shot : shotsList) {
                shotsArray.put(shot);
            }
            testData.put("shots", shotsArray);
            testData.put("time_spent", (75 * 60) - timeRemaining);
            testData.put("date", dateKey);
            testData.put("series_completed", currentSeries + 1);

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(currentShooter + "_" + dateKey + "_test_data", testData.toString());
            editor.apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isTimerRunning = false;
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
	}
