package com.shooterpro.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class LearningActivity extends BaseActivity {

    private SharedPreferences prefs;
    private String currentShooter;
    private TrainingPlan trainingPlan;
    private String currentDay;
    private int currentDayInt, currentMonth, currentYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("ShooterPRO", MODE_PRIVATE);
        currentShooter = prefs.getString("current_shooter", "");
        trainingPlan = TrainingPlan.getInstance();

        // Получаем дату из интента или используем текущую
        int day = getIntent().getIntExtra("day", -1);
        int month = getIntent().getIntExtra("month", -1);
        int year = getIntent().getIntExtra("year", -1);

        Calendar calendar;
        if (day == -1 || month == -1 || year == -1) {
            // Используем текущую дату
            calendar = Calendar.getInstance();
            currentDayInt = calendar.get(Calendar.DAY_OF_MONTH);
            currentMonth = calendar.get(Calendar.MONTH);
            currentYear = calendar.get(Calendar.YEAR);
        } else {
            calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            currentDayInt = day;
            currentMonth = month;
            currentYear = year;
        }

        String[] days = {"ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ"};
        currentDay = days[calendar.get(Calendar.DAY_OF_WEEK) - 1];

        createLearningScreen();
    }

    private void createLearningScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);

        int padding = getAdaptivePadding();
        mainLayout.setPadding(padding, padding, padding, padding);

        TextView title = new TextView(this);
        title.setText("📚 План обучения");
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(getTextSize(20));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dpToPx(8));
        mainLayout.addView(title);

        TextView dayTitle = new TextView(this);
        dayTitle.setText("Сегодня: " + getFullDayName(currentDay));
        dayTitle.setTextColor(0xFFE2E8F0);
        dayTitle.setTextSize(getTextSize(18));
        dayTitle.setGravity(Gravity.CENTER);
        dayTitle.setPadding(0, 0, 0, dpToPx(24));
        mainLayout.addView(dayTitle);

        int totalDuration = trainingPlan.getTotalDurationForDay(currentDay);
        TextView timeInfo = new TextView(this);
        timeInfo.setText("⏱ Общее время: " + totalDuration + " мин");
        timeInfo.setTextColor(0xFFA0AEC0);
        timeInfo.setTextSize(getTextSize(14));
        timeInfo.setGravity(Gravity.CENTER);
        timeInfo.setPadding(0, 0, 0, dpToPx(32));
        mainLayout.addView(timeInfo);

        List<Exercise> todayExercises = trainingPlan.getExercisesForDay(currentDay);

        if (todayExercises.isEmpty()) {
            TextView noExercises = new TextView(this);
            noExercises.setText("На сегодня упражнений нет.\nДень отдыха! 😴");
            noExercises.setTextColor(0xFFA0AEC0);
            noExercises.setTextSize(getTextSize(16));
            noExercises.setGravity(Gravity.CENTER);
            noExercises.setPadding(0, dpToPx(40), 0, dpToPx(40));
            mainLayout.addView(noExercises);
        } else {
            for (int i = 0; i < todayExercises.size(); i++) {
                Exercise exercise = todayExercises.get(i);
                mainLayout.addView(createExerciseCard(exercise, i));
            }
        }

        if (!todayExercises.isEmpty()) {
            Button startTrainingBtn = createButton("▶ Начать выполнение", 0xFF9F7AEA);
            startTrainingBtn.setTextSize(getTextSize(14));
            startTrainingBtn.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						startExerciseTimer();
					}
				});

            LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                getButtonHeight()
            );
            btnParams.setMargins(0, dpToPx(32), 0, 0);
            startTrainingBtn.setLayoutParams(btnParams);
            mainLayout.addView(startTrainingBtn);
        }

        TextView selectDayTitle = new TextView(this);
        selectDayTitle.setText("Выбрать другой день:");
        selectDayTitle.setTextColor(0xFFE2E8F0);
        selectDayTitle.setTextSize(getTextSize(16));
        selectDayTitle.setPadding(0, dpToPx(40), 0, dpToPx(16));
        mainLayout.addView(selectDayTitle);

        LinearLayout daysContainer = new LinearLayout(this);
        daysContainer.setOrientation(LinearLayout.HORIZONTAL);
        daysContainer.setGravity(Gravity.CENTER);

        String[] allDays = {"ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ", "ВС"};
        for (String day : allDays) {
            daysContainer.addView(createDayButton(day));
        }

        mainLayout.addView(daysContainer);

        Button backBtn = createButton("← Назад", 0xFF4A5568);
        backBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});

        LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight()
        );
        backParams.setMargins(0, dpToPx(32), 0, 0);
        backBtn.setLayoutParams(backParams);
        mainLayout.addView(backBtn);

        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }

    private LinearLayout createExerciseCard(Exercise exercise, int index) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundColor(0xFF2A3B5A);
        card.setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12));

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        card.setLayoutParams(cardParams);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView number = new TextView(this);
        number.setText((index + 1) + ". ");
        number.setTextColor(0xFF4FD1C7);
        number.setTextSize(getTextSize(14));
        number.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(number);

        TextView name = new TextView(this);
        name.setText(exercise.getName());
        name.setTextColor(0xFFE2E8F0);
        name.setTextSize(getTextSize(14));
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        header.addView(name);

        card.addView(header);

        LinearLayout infoRow = new LinearLayout(this);
        infoRow.setOrientation(LinearLayout.HORIZONTAL);
        infoRow.setPadding(0, dpToPx(8), 0, dpToPx(8));

        TextView duration = new TextView(this);
        duration.setText("⏱ " + exercise.getDuration() + " мин");
        duration.setTextColor(0xFFA0AEC0);
        duration.setTextSize(getTextSize(12));
        duration.setPadding(0, 0, dpToPx(16), 0);
        infoRow.addView(duration);

        TextView category = new TextView(this);
        category.setText("🏷 " + exercise.getCategory());
        category.setTextColor(exercise.getCategoryColor());
        category.setTextSize(getTextSize(12));
        infoRow.addView(category);

        card.addView(infoRow);

        TextView description = new TextView(this);
        description.setText(exercise.getDescription());
        description.setTextColor(0xFFCBD5E0);
        description.setTextSize(getTextSize(14));
        description.setPadding(0, dpToPx(4), 0, 0);
        card.addView(description);

        return card;
    }

    private Button createDayButton(String day) {
        Button button = new Button(this);
        button.setText(day);
        button.setBackgroundColor(day.equals(currentDay) ? 0xFF4FD1C7 : 0xFF4A5568);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(getTextSize(12));
        button.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        button.setAllCaps(false);

        final String selectedDay = day;
        button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					currentDay = selectedDay;
					createLearningScreen();
				}
			});

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            dpToPx(36)
        );
        params.setMargins(dpToPx(2), 0, dpToPx(2), 0);
        button.setLayoutParams(params);

        return button;
    }

    private String getFullDayName(String shortDay) {
        switch (shortDay) {
            case "ПН": return "Понедельник";
            case "ВТ": return "Вторник";
            case "СР": return "Среда";
            case "ЧТ": return "Четверг";
            case "ПТ": return "Пятница";
            case "СБ": return "Суббота";
            case "ВС": return "Воскресенье";
            default: return shortDay;
        }
    }

    private void startExerciseTimer() {
        Toast.makeText(this, "Таймер упражнений запущен!", Toast.LENGTH_SHORT).show();
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
