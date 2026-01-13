package com.shooterpro.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

public class NotesActivity extends BaseActivity {
    private SharedPreferences prefs;
    private String currentShooter, dateKey;
    private EditText notesInput;
    private int day, month, year;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("ShooterPRO", MODE_PRIVATE);
        currentShooter = prefs.getString("current_shooter", "");

        day = getIntent().getIntExtra("day", 1);
        month = getIntent().getIntExtra("month", 0);
        year = getIntent().getIntExtra("year", 2024);

        dateKey = String.format(Locale.getDefault(), "%02d.%02d.%04d", 
                                day, month + 1, year);

        createNotesScreen();
    }

    private void createNotesScreen() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(0xFF0A192F);
        layout.setGravity(Gravity.CENTER);

        int paddingHorizontal = getAdaptivePadding();
        int paddingVertical = isTablet ? dpToPx(40) : dpToPx(20);
        layout.setPadding(paddingHorizontal, paddingVertical, 
                          paddingHorizontal, paddingVertical);

        TextView title = new TextView(this);
        title.setText("📝 ЗАМЕТКИ К ТРЕНИРОВКЕ");
        title.setTextColor(0xFFF56565);
        title.setTextSize(getTextSize(20));
        title.setGravity(Gravity.CENTER);
        layout.addView(title);

        String[] months = {"января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        String dateText = day + " " + months[month] + " " + year;

        TextView dateView = new TextView(this);
        dateView.setText(dateText);
        dateView.setTextColor(0xFFA0AEC0);
        dateView.setTextSize(getTextSize(14));
        dateView.setGravity(Gravity.CENTER);
        dateView.setPadding(0, dpToPx(8), 0, dpToPx(24));
        layout.addView(dateView);

        notesInput = new EditText(this);
        notesInput.setTextColor(0xFFE2E8F0);
        notesInput.setTextSize(getTextSize(16));
        notesInput.setBackgroundColor(0xFF2A3B5A);
        notesInput.setMinHeight(dpToPx(300));
        notesInput.setPadding(dpToPx(12), dpToPx(12), dpToPx(12), dpToPx(12));
        notesInput.setHint("Запишите свои мысли, наблюдения, улучшения...");
        notesInput.setHintTextColor(0xFF718096);

        String savedNotes = prefs.getString(currentShooter + "_" + dateKey + "_notes", "");
        notesInput.setText(savedNotes);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dpToPx(24));
        notesInput.setLayoutParams(params);
        layout.addView(notesInput);

        TextView hints = new TextView(this);
        hints.setText("Что можно записать:\n• Ощущения во время стрельбы\n• Проблемы с техникой\n• Успехи и достижения\n• Планы на следующую тренировку\n• Погодные условия\n• Самочувствие");
        hints.setTextColor(0xFF718096);
        hints.setTextSize(getTextSize(12));
        hints.setPadding(0, 0, 0, dpToPx(24));
        layout.addView(hints);

        Button saveBtn = createButton("💾 СОХРАНИТЬ", 0xFF4FD1C7);
        saveBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					saveNotes();
				}
			});
        layout.addView(saveBtn);

        Button backBtn = createButton("← НАЗАД", 0xFF4A5568);
        backBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					finish();
				}
			});
        layout.addView(backBtn);

        setContentView(layout);
    }

    private void saveNotes() {
        String notes = notesInput.getText().toString().trim();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(currentShooter + "_" + dateKey + "_notes", notes);
        editor.apply();
        Toast.makeText(this, "Заметки сохранены", Toast.LENGTH_SHORT).show();
        finish();
    }

    private Button createButton(String text, int color) {
        Button button = new Button(this);
        button.setText(text);
        button.setBackgroundColor(color);
        button.setTextColor(0xFFFFFFFF);
        button.setTextSize(getTextSize(16));
        button.setPadding(0, dpToPx(12), 0, dpToPx(12));
        button.setAllCaps(false);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            getButtonHeight());
        params.setMargins(0, 0, 0, dpToPx(12));
        button.setLayoutParams(params);
        return button;
    }
}
