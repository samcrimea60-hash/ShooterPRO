package com.shooterpro.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class StatsActivity extends BaseActivity {

    private SharedPreferences prefs;
    private String currentShooter, dateKey;
    private DecimalFormat df = new DecimalFormat("0.0");
    private int day, month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("ShooterPRO", MODE_PRIVATE);
        currentShooter = prefs.getString("current_shooter", "");

        day = getIntent().getIntExtra("day", 1);
        month = getIntent().getIntExtra("month", 0);
        year = getIntent().getIntExtra("year", 2024);
        dateKey = String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year);

        createStatsScreen();
    }

    private void createStatsScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);

        int padding = getAdaptivePadding();
        mainLayout.setPadding(padding, dpToPx(20), padding, padding);

        String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        String dateText = day + " " + months[month];

        TextView title = new TextView(this);
        title.setText("📊 СТАТИСТИКА ЗА " + dateText);
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(getTextSize(18));
        title.setGravity(Gravity.CENTER);
        mainLayout.addView(title);

        LinearLayout statsContainer = new LinearLayout(this);
        statsContainer.setOrientation(LinearLayout.VERTICAL);
        statsContainer.setBackgroundColor(0xFF2A3B5A);
        statsContainer.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(20), 0, 0);
        statsContainer.setLayoutParams(params);

        boolean hasTraining = prefs.getBoolean(currentShooter + "_" + dateKey + "_has_training", false);
        boolean hasTest = prefs.getBoolean(currentShooter + "_" + dateKey + "_has_test", false);

        if (!hasTraining && !hasTest) {
            TextView noData = new TextView(this);
            noData.setText("Нет данных за этот день");
            noData.setTextColor(0xFFA0AEC0);
            noData.setTextSize(getTextSize(16));
            noData.setGravity(Gravity.CENTER);
            noData.setPadding(0, dpToPx(20), 0, dpToPx(20));
            statsContainer.addView(noData);
        } else {
            if (hasTraining) {
                addTrainingStats(statsContainer);
            }

            if (hasTest) {
                if (hasTraining) {
                    TextView separator = new TextView(this);
                    separator.setText("────────────");
                    separator.setTextColor(0xFF4A5568);
                    separator.setTextSize(getTextSize(14));
                    separator.setGravity(Gravity.CENTER);
                    separator.setPadding(0, dpToPx(20), 0, dpToPx(20));
                    statsContainer.addView(separator);
                }

                addTestStats(statsContainer);
            }

            String notes = prefs.getString(currentShooter + "_" + dateKey + "_notes", "");
            if (!notes.isEmpty()) {
                TextView notesTitle = new TextView(this);
                notesTitle.setText("\nЗАМЕТКИ:");
                notesTitle.setTextColor(0xFFA0AEC0);
                notesTitle.setTextSize(getTextSize(14));
                notesTitle.setPadding(0, dpToPx(20), 0, dpToPx(10));
                statsContainer.addView(notesTitle);

                TextView notesText = new TextView(this);
                notesText.setText(notes);
                notesText.setTextColor(0xFFE2E8F0);
                notesText.setTextSize(getTextSize(14));
                notesText.setPadding(0, 0, 0, dpToPx(10));
                statsContainer.addView(notesText);
            }
        }

        mainLayout.addView(statsContainer);

        Button backBtn = createButton("← НАЗАД", 0xFF4A5568);
        backBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

        params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight()
        );
        params.setMargins(0, dpToPx(20), 0, 0);
        backBtn.setLayoutParams(params);
        mainLayout.addView(backBtn);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private void addTrainingStats(LinearLayout container) {
        TextView trainingTitle = new TextView(this);
        trainingTitle.setText("🏁 ТРЕНИРОВКА");
        trainingTitle.setTextColor(0xFF4FD1C7);
        trainingTitle.setTextSize(getTextSize(16));
        trainingTitle.setPadding(0, 0, 0, dpToPx(15));
        container.addView(trainingTitle);

        // ПЫТАЕМСЯ ПОЛУЧИТЬ ДАННЫЕ ТРЕНИРОВКИ ИЗ ДВУХ МЕСТ
        String trainingDataStr = prefs.getString(currentShooter + "_" + dateKey + "_training_data", "");

        // Если не нашли по дате, пробуем старый формат (для совместимости)
        if (trainingDataStr.isEmpty()) {
            // Проверяем, есть ли данные в старом формате для этой даты
            String oldDataStr = prefs.getString(currentShooter + "_training_data", "");
            String trainingDate = prefs.getString(currentShooter + "_training_date", "");

            if (!oldDataStr.isEmpty() && dateKey.equals(trainingDate)) {
                trainingDataStr = oldDataStr;
            }
        }

        if (!trainingDataStr.isEmpty()) {
            try {
                JSONObject trainingData = new JSONObject(trainingDataStr);
                JSONArray shotsArray = trainingData.getJSONArray("shots");
                ArrayList<Float> shotsList = new ArrayList<>();

                for (int i = 0; i < shotsArray.length(); i++) {
                    shotsList.add((float) shotsArray.getDouble(i));
                }

                float totalScore = 0;
                float bestShot = 0;
                float worstShot = 11.0f;
                int breaks = 0;
                float[] seriesSums = new float[6];

                for (int i = 0; i < shotsList.size(); i++) {
                    float shot = shotsList.get(i);
                    totalScore += shot;
                    if (shot > bestShot) bestShot = shot;
                    if (shot < worstShot) worstShot = shot;
                    if (shot < 9.0f) breaks++;

                    int seriesIndex = i / 10;
                    if (seriesIndex < 6) {
                        seriesSums[seriesIndex] += shot;
                    }
                }

                addStatRow(container, "Выстрелов:", String.valueOf(shotsList.size()));
                addStatRow(container, "Общий результат:", df.format(totalScore));

                for (int i = 0; i < 6; i++) {
                    if (seriesSums[i] > 0) {
                        addStatRow(container, "Серия " + (i + 1) + ":", df.format(seriesSums[i]));
                    }
                }

                addStatRow(container, "Лучший выстрел:", df.format(bestShot));
                addStatRow(container, "Худший выстрел:", df.format(worstShot));
                addStatRow(container, "Отрывов (<9.0):", String.valueOf(breaks));

                long startTime = trainingData.optLong("start_time", 0);
                if (startTime > 0) {
                    long timeSpent = (System.currentTimeMillis() - startTime) / 1000;
                    addStatRow(container, "Время:", formatTime(timeSpent));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                addErrorRow(container, "Ошибка загрузки данных тренировки");
            }
        } else {
            addErrorRow(container, "Данные тренировки не найдены");
        }
    }

    private void addTestStats(LinearLayout container) {
        TextView testTitle = new TextView(this);
        testTitle.setText("🎯 ЗАЧЕТ");
        testTitle.setTextColor(0xFFECC94B);
        testTitle.setTextSize(getTextSize(16));
        testTitle.setPadding(0, 0, 0, dpToPx(15));
        container.addView(testTitle);

        String testDataStr = prefs.getString(currentShooter + "_" + dateKey + "_test_data", "");
        if (!testDataStr.isEmpty()) {
            try {
                JSONObject testData = new JSONObject(testDataStr);
                JSONArray shotsArray = testData.getJSONArray("shots");
                ArrayList<Float> shotsList = new ArrayList<>();

                for (int i = 0; i < shotsArray.length(); i++) {
                    shotsList.add((float) shotsArray.getDouble(i));
                }

                float totalScore = 0;
                float bestShot = 0;
                float worstShot = 11.0f;
                int breaks = 0;
                float[] seriesSums = new float[6];

                for (int i = 0; i < shotsList.size(); i++) {
                    float shot = shotsList.get(i);
                    totalScore += shot;
                    if (shot > bestShot) bestShot = shot;
                    if (shot < worstShot) worstShot = shot;
                    if (shot < 9.0f) breaks++;

                    int seriesIndex = i / 10;
                    if (seriesIndex < 6) {
                        seriesSums[seriesIndex] += shot;
                    }
                }

                addStatRow(container, "Выстрелов:", String.valueOf(shotsList.size()));
                addStatRow(container, "Общий результат:", df.format(totalScore));

                for (int i = 0; i < 6; i++) {
                    if (seriesSums[i] > 0) {
                        addStatRow(container, "Серия " + (i + 1) + ":", df.format(seriesSums[i]));
                    }
                }

                addStatRow(container, "Лучший выстрел:", df.format(bestShot));
                addStatRow(container, "Худший выстрел:", df.format(worstShot));
                addStatRow(container, "Отрывов (<9.0):", String.valueOf(breaks));

                long timeSpent = testData.getLong("time_spent");
                addStatRow(container, "Время:", formatTime(timeSpent));
            } catch (JSONException e) {
                e.printStackTrace();
                addErrorRow(container, "Ошибка загрузки данных зачета");
            }
        } else {
            addErrorRow(container, "Данные зачета не найдены");
        }
    }

    private void addStatRow(LinearLayout container, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFFA0AEC0);
        labelView.setTextSize(getTextSize(14));
        labelView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));
        row.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFE2E8F0);
        valueView.setTextSize(getTextSize(14));
        valueView.setGravity(View.TEXT_ALIGNMENT_TEXT_END);
        valueView.setLayoutParams(new LinearLayout.LayoutParams(0,
                                                                LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));
        row.addView(valueView);

        container.addView(row);
    }

    private void addErrorRow(LinearLayout container, String errorMessage) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView errorView = new TextView(this);
        errorView.setText(errorMessage);
        errorView.setTextColor(0xFFF56565);
        errorView.setTextSize(getTextSize(12));
        row.addView(errorView);

        container.addView(row);
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    private Button createButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(getTextSize(14));
        button.setPadding(0, dpToPx(12), 0, dpToPx(12));
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight()
        );
        params.setMargins(0, dpToPx(8), 0, 0);
        button.setLayoutParams(params);

        return button;
    }
	}
