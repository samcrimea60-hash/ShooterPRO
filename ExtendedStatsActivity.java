package com.shooterpro.app;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.shooterpro.app.data.ShooterDataManager;
import com.shooterpro.app.database.DatabaseHelper;
import com.shooterpro.app.export.PDFExporter;
import com.shooterpro.app.ui.ButtonFactory;
import com.shooterpro.app.ui.charts.BarChartView;
import com.shooterpro.app.ui.charts.LineChartView;
import com.shooterpro.app.utils.DisplayUtils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExtendedStatsActivity extends BaseActivity {
    
    private ShooterDataManager dataManager;
    private String currentShooter;
    private DecimalFormat df = new DecimalFormat("0.0");
    private DecimalFormat df2 = new DecimalFormat("0.00");
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        dataManager = ShooterDataManager.getInstance(this);
        currentShooter = dataManager.getCurrentShooter();
        
        createExtendedStatsScreen();
    }
    
    private void createExtendedStatsScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xFF0A192F);
        
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(0xFF0A192F);
        
        int padding = DisplayUtils.dpToPx(this, 16);
        mainLayout.setPadding(padding, DisplayUtils.dpToPx(this, 20), padding, padding);
        
        // Заголовок
        TextView title = new TextView(this);
        title.setText("📊 РАСШИРЕННАЯ СТАТИСТИКА");
        title.setTextColor(0xFF4FD1C7);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, DisplayUtils.dpToPx(this, 20));
        mainLayout.addView(title);
        
        // Информация о спортсмене
        TextView shooterInfo = new TextView(this);
        shooterInfo.setText("Спортсмен: " + currentShooter);
        shooterInfo.setTextColor(0xFFA0AEC0);
        shooterInfo.setTextSize(16);
        shooterInfo.setGravity(Gravity.CENTER);
        shooterInfo.setPadding(0, 0, 0, DisplayUtils.dpToPx(this, 20));
        mainLayout.addView(shooterInfo);
        
        try {
            DatabaseHelper.TrainingStats stats = dataManager.getStatistics(currentShooter);
            
            // Кнопка экспорта
            Button exportBtn = ButtonFactory.createSmallButton(this, "📤 Экспорт отчета (TXT)", 0xFFECC94B);
            exportBtn.setOnClickListener(v -> exportReport());
            mainLayout.addView(exportBtn);
            
            // Блок общей статистики
            mainLayout.addView(createOverallStatsBlock(stats));
            
            // График прогресса (заглушка)
            mainLayout.addView(createProgressChart());
            
            // Блок лучших результатов
            mainLayout.addView(createBestResultsBlock());
            
            // График распределения баллов
            mainLayout.addView(createDistributionChart());
            
            // Рекомендации
            mainLayout.addView(createRecommendationsBlock(stats));
            
        } catch (Exception e) {
            e.printStackTrace();
            TextView errorText = new TextView(this);
            errorText.setText("Ошибка загрузки статистики. Возможно, данных нет.");
            errorText.setTextColor(0xFFF56565);
            errorText.setTextSize(14);
            errorText.setGravity(Gravity.CENTER);
            errorText.setPadding(0, DisplayUtils.dpToPx(this, 20), 0, DisplayUtils.dpToPx(this, 20));
            mainLayout.addView(errorText);
        }
        
        // Кнопка назад
        Button backBtn = ButtonFactory.createBackButton(this, "← НАЗАД");
        backBtn.setOnClickListener(v -> finish());
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 52)
        );
        params.setMargins(0, DisplayUtils.dpToPx(this, 20), 0, 0);
        backBtn.setLayoutParams(params);
        mainLayout.addView(backBtn);
        
        scrollView.addView(mainLayout);
        setContentView(scrollView);
    }
    
    private LinearLayout createOverallStatsBlock(DatabaseHelper.TrainingStats stats) {
        LinearLayout block = createBlockContainer("📈 ОБЩАЯ СТАТИСТИКА");
        
        if (stats.getTotalTrainings() == 0) {
            TextView noData = new TextView(this);
            noData.setText("Нет данных для отображения");
            noData.setTextColor(0xFFA0AEC0);
            noData.setTextSize(14);
            noData.setGravity(Gravity.CENTER);
            noData.setPadding(0, DisplayUtils.dpToPx(this, 20), 0, DisplayUtils.dpToPx(this, 20));
            block.addView(noData);
            return block;
        }
        
        addStatRow(block, "Всего тренировок:", String.valueOf(stats.getTotalTrainings()));
        addStatRow(block, "Всего выстрелов:", String.valueOf(stats.getTotalShots()));
        addStatRow(block, "Средний балл:", df.format(stats.getOverallAverage()));
        addStatRow(block, "Последняя тренировка:", 
                  stats.getLastTraining() != null ? stats.getLastTraining() : "нет данных");
        
        // Расчет интенсивности
        if (stats.getTotalTrainings() > 0) {
            float shotsPerTraining = (float) stats.getTotalShots() / stats.getTotalTrainings();
            addStatRow(block, "Среднее выстрелов за тренировку:", df.format(shotsPerTraining));
        }
        
        return block;
    }
    
    private LinearLayout createProgressChart() {
        LinearLayout block = createBlockContainer("📈 ПРОГРЕСС");
        
        // Создаем график
        LineChartView chart = new LineChartView(this);
        chart.setTitle("Динамика среднего балла");
        
        // Тестовые данные (в реальном приложении брать из БД)
        List<Float> testData = new ArrayList<>();
        testData.add(8.5f);
        testData.add(8.7f);
        testData.add(9.0f);
        testData.add(9.2f);
        testData.add(9.5f);
        testData.add(9.3f);
        testData.add(9.6f);
        
        chart.setData(testData);
        
        // Устанавливаем размеры графика
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 300)
        );
        chart.setLayoutParams(params);
        
        block.addView(chart);
        
        TextView note = new TextView(this);
        note.setText("📊 График показывает динамику вашего прогресса");
        note.setTextColor(0xFF718096);
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        note.setPadding(0, DisplayUtils.dpToPx(this, 12), 0, 0);
        block.addView(note);
        
        return block;
    }
    
    private LinearLayout createBestResultsBlock() {
        LinearLayout block = createBlockContainer("🏆 ЛУЧШИЕ РЕЗУЛЬТАТЫ");
        
        // Тестовые данные
        List<String[]> bestResults = new ArrayList<>();
        bestResults.add(new String[]{"15.01.2024", "Тренировка", "9.8", "60"});
        bestResults.add(new String[]{"22.01.2024", "Зачет", "9.6", "60"});
        bestResults.add(new String[]{"05.02.2024", "Тренировка", "9.5", "50"});
        
        for (int i = 0; i < bestResults.size(); i++) {
            String[] result = bestResults.get(i);
            
            LinearLayout resultRow = new LinearLayout(this);
            resultRow.setOrientation(LinearLayout.HORIZONTAL);
            resultRow.setBackgroundColor(0xFF1A2B4A);
            resultRow.setPadding(DisplayUtils.dpToPx(this, 12), 
                               DisplayUtils.dpToPx(this, 12),
                               DisplayUtils.dpToPx(this, 12),
                               DisplayUtils.dpToPx(this, 12));
            
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.setMargins(0, 0, 0, DisplayUtils.dpToPx(this, 8));
            resultRow.setLayoutParams(rowParams);
            
            TextView rank = new TextView(this);
            rank.setText((i + 1) + ". ");
            rank.setTextColor(0xFFECC94B);
            rank.setTextSize(14);
            rank.setLayoutParams(new LinearLayout.LayoutParams(
                DisplayUtils.dpToPx(this, 30), 
                LinearLayout.LayoutParams.WRAP_CONTENT));
            resultRow.addView(rank);
            
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            info.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            
            TextView dateText = new TextView(this);
            dateText.setText(result[0] + " • " + result[1]);
            dateText.setTextColor(0xFFE2E8F0);
            dateText.setTextSize(14);
            info.addView(dateText);
            
            TextView details = new TextView(this);
            details.setText(result[2] + " средний • " + result[3] + " выстр.");
            details.setTextColor(0xFFA0AEC0);
            details.setTextSize(12);
            info.addView(details);
            
            resultRow.addView(info);
            block.addView(resultRow);
        }
        
        return block;
    }
    
    private LinearLayout createDistributionChart() {
        LinearLayout block = createBlockContainer("📊 РАСПРЕДЕЛЕНИЕ БАЛЛОВ");
        
        BarChartView chart = new BarChartView(this);
        chart.setTitle("Частота баллов");
        
        // Тестовые данные
        List<BarChartView.BarData> barData = new ArrayList<>();
        barData.add(new BarChartView.BarData("<9.0", 15, Color.parseColor("#FFF56565")));
        barData.add(new BarChartView.BarData("9.0-9.4", 25, Color.parseColor("#FFECC94B")));
        barData.add(new BarChartView.BarData("9.5-9.9", 40, Color.parseColor("#FF9F7AEA")));
        barData.add(new BarChartView.BarData("10.0+", 20, Color.parseColor("#FF4FD1C7")));
        
        chart.setData(barData);
        
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            DisplayUtils.dpToPx(this, 400)
        );
        chart.setLayoutParams(params);
        
        block.addView(chart);
        
        return block;
    }
    
    private LinearLayout createRecommendationsBlock(DatabaseHelper.TrainingStats stats) {
        LinearLayout block = createBlockContainer("💡 РЕКОМЕНДАЦИИ");
        
        TextView recommendations = new TextView(this);
        recommendations.setText(getPersonalizedRecommendations(stats));
        recommendations.setTextColor(0xFFE2E8F0);
        recommendations.setTextSize(14);
        recommendations.setPadding(0, DisplayUtils.dpToPx(this, 8), 0, 0);
        block.addView(recommendations);
        
        return block;
    }
    
    private String getPersonalizedRecommendations(DatabaseHelper.TrainingStats stats) {
        StringBuilder rec = new StringBuilder();
        
        if (stats.getTotalTrainings() == 0) {
            return "Начните первую тренировку! 💪\n\nРекомендации появятся после набора данных.";
        }
        
        if (stats.getTotalTrainings() < 5) {
            rec.append("• Необходимо больше тренировок для анализа (сейчас: ")
               .append(stats.getTotalTrainings()).append(")\n");
        }
        
        if (stats.getOverallAverage() < 8.0) {
            rec.append("• Сосредоточьтесь на базовой технике\n");
            rec.append("• Уделяйте больше времени разминке\n");
            rec.append("• Практикуйте дыхательные упражнения\n");
        } else if (stats.getOverallAverage() < 9.0) {
            rec.append("• Хороший прогресс! Продолжайте тренировки\n");
            rec.append("• Работайте над стабильностью выстрелов\n");
            rec.append("• Анализируйте каждый отрыв (<9.0)\n");
        } else if (stats.getOverallAverage() < 9.5) {
            rec.append("• Отличные результаты!\n");
            rec.append("• Увеличивайте количество выстрелов за тренировку\n");
            rec.append("• Практикуйтесь в условиях стресса\n");
        } else {
            rec.append("• Профессиональный уровень! 🏆\n");
            rec.append("• Подготовьтесь к соревнованиям\n");
            rec.append("• Помогайте другим стрелкам\n");
        }
        
        rec.append("\nОбщие советы:\n");
        rec.append("• Тренируйтесь регулярно (3-4 раза в неделю)\n");
        rec.append("• Ведите дневник тренировок\n");
        rec.append("• Соблюдайте режим отдыха\n");
        rec.append("• Питайтесь правильно перед тренировками\n");
        
        return rec.toString();
    }
    
    private LinearLayout createBlockContainer(String titleText) {
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
    
    private void addStatRow(LinearLayout container, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, DisplayUtils.dpToPx(this, 8), 0, DisplayUtils.dpToPx(this, 8));
        
        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextColor(0xFFA0AEC0);
        labelView.setTextSize(14);
        labelView.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f));
        row.addView(labelView);
        
        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextColor(0xFFE2E8F0);
        valueView.setTextSize(14);
        valueView.setGravity(Gravity.END);
        valueView.setLayoutParams(new LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f));
        row.addView(valueView);
        
        container.addView(row);
    }
    
    private void exportReport() {
        PDFExporter exporter = new PDFExporter(this);
        boolean success = exporter.exportStatisticsToTXT(currentShooter);
        
        if (success) {
            // Показать сообщение об успехе
        }
    }
}
