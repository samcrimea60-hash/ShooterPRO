package com.shooterpro.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class TimerActivity extends BaseActivity {

    private SharedPreferences prefs;
    private String currentShooter;
    private List<TimerItem> timers = new ArrayList<>();
    private int currentTimerIndex = 0;
    private Handler handler = new Handler();
    private Runnable timerRunnable;
    private int timeLeft = 0;
    private boolean isRunning = false;
    private TextView timerTitle, timerText, progressText;
    private Button startButton, pauseButton;

    class TimerItem {
        String name;
        int duration;

        TimerItem(String name, int duration) {
            this.name = name;
            this.duration = duration;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefs = getSharedPreferences("ShooterPRO", MODE_PRIVATE);
        currentShooter = prefs.getString("current_shooter", "");

        loadTimers();

        if (timers.isEmpty()) {
            Toast.makeText(this, "Таймеры не настроены. Переход к тренировке", 
						   Toast.LENGTH_SHORT).show();
            goToTraining();
            return;
        }

        createTimerScreen();
        setupCurrentTimer();
    }

    private void loadTimers() {
        // ТОЛЬКО пользовательские таймеры из настроек
        String customTimersStr = prefs.getString("custom_timers", "");
        if (!customTimersStr.isEmpty()) {
            String[] timerArray = customTimersStr.split(";");
            for (String timerStr : timerArray) {
                String[] parts = timerStr.split(":");
                if (parts.length == 3) {
                    try {
                        String name = parts[0];
                        int minutes = Integer.parseInt(parts[1]);
                        int seconds = Integer.parseInt(parts[2]);
                        int totalSeconds = minutes * 60 + seconds;

                        if (totalSeconds > 0) {
                            timers.add(new TimerItem(name, totalSeconds));
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void createTimerScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0A192F);
        layout.setGravity(Gravity.CENTER);

        int paddingHorizontal = getAdaptivePadding();
        int paddingVertical = isTablet ? dpToPx(40) : dpToPx(20);
        layout.setPadding(paddingHorizontal, paddingVertical, paddingHorizontal, paddingVertical);

        TextView title = new TextView(this);
        title.setText("⏱ ПОДГОТОВИТЕЛЬНЫЕ ТАЙМЕРЫ");
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(getTextSize(20));
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, dpToPx(20));
        layout.addView(title);

        timerTitle = new TextView(this);
        timerTitle.setTextColor(0xFFE2E8F0);
        timerTitle.setTextSize(getTextSize(18));
        timerTitle.setGravity(Gravity.CENTER);
        layout.addView(timerTitle);

        timerText = new TextView(this);
        timerText.setTextColor(0xFFE2E8F0);
        timerText.setTextSize(getTextSize(48));
        timerText.setGravity(Gravity.CENTER);
        timerText.setPadding(0, dpToPx(20), 0, dpToPx(40));
        layout.addView(timerText);

        progressText = new TextView(this);
        progressText.setText("Таймер " + (currentTimerIndex + 1) + " из " + timers.size());
        progressText.setTextColor(0xFFA0AEC0);
        progressText.setTextSize(getTextSize(14));
        progressText.setGravity(Gravity.CENTER);
        progressText.setPadding(0, 0, 0, dpToPx(20));
        layout.addView(progressText);

        TextView instruction = new TextView(this);
        instruction.setText("Нажмите 'Запустить' для начала\nКаждый таймер запускается вручную");
        instruction.setTextColor(0xFF718096);
        instruction.setTextSize(getTextSize(12));
        instruction.setGravity(Gravity.CENTER);
        instruction.setPadding(0, 0, 0, dpToPx(20));
        layout.addView(instruction);

        startButton = createTimerButton("▶ ЗАПУСТИТЬ", 0xFF4FD1C7);
        startButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startTimer();
				}
			});
        layout.addView(startButton);

        pauseButton = createTimerButton("⏸ ПАУЗА", 0xFFECC94B);
        pauseButton.setVisibility(View.GONE);
        pauseButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					pauseTimer();
				}
			});
        layout.addView(pauseButton);

        Button skipButton = createTimerButton("⏭ ПРОПУСТИТЬ ТАЙМЕР", 0xFF4A5568);
        skipButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					skipTimer();
				}
			});
        layout.addView(skipButton);

        Button skipAllButton = createTimerButton("🚀 ПРОПУСТИТЬ ВСЕ ТАЙМЕРЫ", 0xFFF56565);
        skipAllButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					skipAllTimers();
				}
			});

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight()
        );
        params.setMargins(0, dpToPx(10), 0, 0);
        skipAllButton.setLayoutParams(params);
        layout.addView(skipAllButton);

        setContentView(layout);
    }

    private void setupCurrentTimer() {
        if (currentTimerIndex < timers.size()) {
            TimerItem timer = timers.get(currentTimerIndex);
            timerTitle.setText(timer.name);
            timeLeft = timer.duration;
            updateTimerText();
            progressText.setText("Таймер " + (currentTimerIndex + 1) + " из " + timers.size());
        } else {
            goToTraining();
        }
    }

    private void updateTimerText() {
        int minutes = timeLeft / 60;
        int seconds = timeLeft % 60;
        timerText.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void startTimer() {
        if (!isRunning) {
            isRunning = true;
            startButton.setVisibility(View.GONE);
            pauseButton.setVisibility(View.VISIBLE);

            timerRunnable = new Runnable() {
                @Override
                public void run() {
                    if (isRunning && timeLeft > 0) {
                        timeLeft--;
                        updateTimerText();
                        handler.postDelayed(this, 1000);

                        if (timeLeft == 0) {
                            isRunning = false;
                            pauseButton.setVisibility(View.GONE);

                            Toast.makeText(TimerActivity.this, 
										   "Таймер '" + timers.get(currentTimerIndex).name + "' завершен!",
										   Toast.LENGTH_SHORT).show();

                            currentTimerIndex++;

                            if (currentTimerIndex < timers.size()) {
                                setupCurrentTimer();
                                startButton.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(TimerActivity.this, 
											   "Все подготовительные таймеры завершены!",
											   Toast.LENGTH_SHORT).show();

                                handler.postDelayed(new Runnable() {
										@Override
										public void run() {
											goToTraining();
										}
									}, 1500);
                            }
                        }
                    }
                }
            };
            handler.postDelayed(timerRunnable, 1000);
        }
    }

    private void pauseTimer() {
        if (isRunning) {
            isRunning = false;
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);

            if (timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
        }
    }

    private void skipTimer() {
        if (isRunning) {
            isRunning = false;
            if (timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
        }

        currentTimerIndex++;

        if (currentTimerIndex < timers.size()) {
            setupCurrentTimer();
            startButton.setVisibility(View.VISIBLE);
            pauseButton.setVisibility(View.GONE);
            Toast.makeText(this, "Таймер пропущен", Toast.LENGTH_SHORT).show();
        } else {
            goToTraining();
        }
    }

    private void skipAllTimers() {
        if (isRunning) {
            isRunning = false;
            if (timerRunnable != null) {
                handler.removeCallbacks(timerRunnable);
            }
        }

        Toast.makeText(this, "Все таймеры пропущены", Toast.LENGTH_SHORT).show();
        goToTraining();
    }

    private void goToTraining() {
        Intent intent = new Intent(this, TrainingActivity.class);

        Intent originalIntent = getIntent();
        if (originalIntent != null) {
            int day = originalIntent.getIntExtra("day", 1);
            int month = originalIntent.getIntExtra("month", 0);
            int year = originalIntent.getIntExtra("year", 2024);

            intent.putExtra("day", day);
            intent.putExtra("month", month);
            intent.putExtra("year", year);
            intent.putExtra("continue_training", false);
        }

        startActivity(intent);
        finish();
    }

    private Button createTimerButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(getTextSize(16));
        button.setPadding(0, dpToPx(12), 0, dpToPx(12));
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight()
        );
        params.setMargins(0, 0, 0, dpToPx(12));
        button.setLayoutParams(params);

        return button;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timerRunnable != null) {
            handler.removeCallbacks(timerRunnable);
        }
    }
}
