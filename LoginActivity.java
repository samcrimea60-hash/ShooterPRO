package com.shooterpro.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class LoginActivity extends BaseActivity {
    private SharedPreferences prefs;
    private Spinner shooterSpinner;
    private EditText newShooterEditText;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ВАЖНО: Используем XML-макет вместо программного создания
        setContentView(R.layout.activity_login);

        prefs = getSharedPreferences("ShooterPRO", MODE_PRIVATE);

        // Находим элементы из XML
        shooterSpinner = findViewById(R.id.shooterSpinner);
        newShooterEditText = findViewById(R.id.newShooterEditText);
        startButton = findViewById(R.id.startButton);

        loadShootersList();

        startButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					startApp();
				}
			});
    }

    private void loadShootersList() {
        // Получаем список стрелков из SharedPreferences
        Set<String> shootersSet = prefs.getStringSet("shooters_list", new HashSet<String>());
        ArrayList<String> shootersList = new ArrayList<>(shootersSet);

        // Если список пустой, добавляем стандартного пользователя
        if (shootersList.isEmpty()) {
            shootersList.add("Иван Иванов");
        }

        // Создаем адаптер для Spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            shootersList
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Устанавливаем адаптер для Spinner
        shooterSpinner.setAdapter(adapter);
    }

    private void startApp() {
        // 1. Получаем выбранного стрелка из Spinner
        String selectedShooter = (String) shooterSpinner.getSelectedItem();

        // 2. Сохраняем имя текущего стрелка в SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("current_shooter", selectedShooter);

        // 3. Если в поле ввода есть текст, добавляем нового стрелка в список
        String newShooterName = newShooterEditText.getText().toString().trim();
        if (!newShooterName.isEmpty()) {
            Set<String> currentShooters = prefs.getStringSet("shooters_list", new HashSet<String>());
            Set<String> updatedShooters = new HashSet<>(currentShooters);
            updatedShooters.add(newShooterName);

            editor.putStringSet("shooters_list", updatedShooters);
            editor.putString("current_shooter", newShooterName);
        }

        editor.apply();

        // 4. Переходим на главный экран
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}
