package com.shooterpro.app.export;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.shooterpro.app.database.DatabaseHelper;
import com.shooterpro.app.models.TrainingSession;
import com.shooterpro.app.repository.TrainingRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFExporter {
    
    private Context context;
    private TrainingRepository repository;
    
    public PDFExporter(Context context) {
        this.context = context;
        this.repository = new TrainingRepository(context);
    }
    
    public boolean exportStatisticsToTXT(String shooterName) {
        try {
            DatabaseHelper.TrainingStats stats = repository.getStatistics(shooterName);
            long shooterId = repository.getShooterId(shooterName);
            
            if (shooterId == -1) {
                Toast.makeText(context, "Спортсмен не найден", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "ShooterPRO_" + shooterName + "_" + timestamp + ".txt";
            
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "ShooterPRO");
            
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, fileName);
            
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            
            // Заголовок
            osw.write("========================================\n");
            osw.write("            ОТЧЕТ ShooterPRO            \n");
            osw.write("========================================\n\n");
            
            // Информация о спортсмене
            osw.write("СПОРТСМЕН: " + shooterName + "\n");
            osw.write("Дата отчета: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\n\n");
            
            // Общая статистика
            osw.write("------ ОБЩАЯ СТАТИСТИКА ------\n");
            osw.write("Всего тренировок: " + stats.getTotalTrainings() + "\n");
            osw.write("Всего выстрелов: " + stats.getTotalShots() + "\n");
            osw.write("Средний балл: " + String.format(Locale.getDefault(), "%.2f", stats.getOverallAverage()) + "\n");
            osw.write("Последняя тренировка: " + (stats.getLastTraining() != null ? stats.getLastTraining() : "нет данных") + "\n\n");
            
            // Подробная статистика по дням
            osw.write("------ ДЕТАЛЬНАЯ СТАТИСТИКА ------\n");
            // Здесь можно добавить детальные данные из БД
            
            // Рекомендации
            osw.write("\n------ РЕКОМЕНДАЦИИ ------\n");
            osw.write(getRecommendations(stats));
            
            osw.write("\n========================================\n");
            osw.write("Сгенерировано приложением ShooterPRO\n");
            osw.write("© 2024 Все права защищены\n");
            osw.write("========================================\n");
            
            osw.close();
            fos.close();
            
            Toast.makeText(context, "Отчет сохранен: " + file.getAbsolutePath(), 
                          Toast.LENGTH_LONG).show();
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка при создании отчета: " + e.getMessage(), 
                          Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    private String getRecommendations(DatabaseHelper.TrainingStats stats) {
        StringBuilder recommendations = new StringBuilder();
        
        if (stats.getTotalTrainings() < 5) {
            recommendations.append("• Рекомендуется больше тренироваться (минимум 5 тренировок)\n");
        }
        
        if (stats.getOverallAverage() < 8.0) {
            recommendations.append("• Необходимо улучшить стабильность выстрелов\n");
            recommendations.append("• Сконцентрируйтесь на технике дыхания\n");
        } else if (stats.getOverallAverage() < 9.5) {
            recommendations.append("• Хорошие результаты! Продолжайте в том же духе\n");
            recommendations.append("• Работайте над уменьшением количества отрывов\n");
        } else {
            recommendations.append("• Отличные результаты! Профессиональный уровень\n");
            recommendations.append("• Рекомендуется участие в соревнованиях\n");
        }
        
        recommendations.append("• Регулярность тренировок - ключ к успеху\n");
        recommendations.append("• Анализируйте свои ошибки после каждой тренировки\n");
        
        return recommendations.toString();
    }
    
    public boolean exportToCSV(String shooterName) {
        try {
            long shooterId = repository.getShooterId(shooterName);
            if (shooterId == -1) return false;
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "ShooterPRO_" + shooterName + "_" + timestamp + ".csv";
            
            File dir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "ShooterPRO");
            
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, fileName);
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            
            // Заголовок CSV
            osw.write("Дата;Тип;Выстрелов;Общий балл;Средний балл;Лучший;Худший;Отрывов;Время;Заметки\n");
            
            // Здесь можно добавить данные из БД
            
            osw.close();
            fos.close();
            
            Toast.makeText(context, "CSV файл сохранен", Toast.LENGTH_SHORT).show();
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void cleanupOldReports() {
        File dir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOCUMENTS), "ShooterPRO");
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null && files.length > 10) {
                // Удаляем самые старые файлы, оставляя только 10 последних
                // Реализация может быть добавлена при необходимости
            }
        }
    }
}
