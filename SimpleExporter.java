package com.shooterpro.app.export;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.shooterpro.app.data.ShooterDataManager;
import com.shooterpro.app.database.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SimpleExporter {
    
    private Context context;
    
    public SimpleExporter(Context context) {
        this.context = context;
    }
    
    public boolean exportToTxt(String shooterName) {
        try {
            ShooterDataManager dataManager = ShooterDataManager.getInstance(context);
            DatabaseHelper.TrainingStats stats = dataManager.getStatistics(shooterName);
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "ShooterPRO_" + shooterName + "_" + timestamp + ".txt";
            
            // Пытаемся сохранить в разные места
            File file = findWritableFile(fileName);
            
            if (file == null) {
                Toast.makeText(context, "Не удалось сохранить файл", Toast.LENGTH_SHORT).show();
                return false;
            }
            
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
            
            // Создаем отчет
            osw.write("=== ОТЧЕТ ShooterPRO ===\n");
            osw.write("Спортсмен: " + shooterName + "\n");
            osw.write("Дата: " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date()) + "\n\n");
            
            osw.write("--- СТАТИСТИКА ---\n");
            osw.write("Всего тренировок: " + stats.getTotalTrainings() + "\n");
            osw.write("Всего выстрелов: " + stats.getTotalShots() + "\n");
            osw.write("Средний балл: " + String.format(Locale.getDefault(), "%.2f", stats.getOverallAverage()) + "\n");
            osw.write("Последняя тренировка: " + (stats.getLastTraining() != null ? stats.getLastTraining() : "нет данных") + "\n\n");
            
            osw.write("--- РЕКОМЕНДАЦИИ ---\n");
            osw.write(getRecommendations(stats));
            
            osw.write("\n=== КОНЕЦ ОТЧЕТА ===\n");
            osw.write("Сгенерировано приложением ShooterPRO\n");
            
            osw.close();
            fos.close();
            
            Toast.makeText(context, "Отчет сохранен: " + file.getAbsolutePath(), 
                          Toast.LENGTH_LONG).show();
            return true;
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }
    
    private File findWritableFile(String fileName) {
        // Пробуем разные места для сохранения
        
        // 1. Внутренняя память приложения
        File internalDir = context.getFilesDir();
        File internalFile = new File(internalDir, fileName);
        if (canWriteToFile(internalFile)) {
            return internalFile;
        }
        
        // 2. Внешняя память (если доступна)
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS);
            
            if (!externalDir.exists()) {
                externalDir.mkdirs();
            }
            
            File externalFile = new File(externalDir, "ShooterPRO/" + fileName);
            externalFile.getParentFile().mkdirs();
            
            if (canWriteToFile(externalFile)) {
                return externalFile;
            }
        }
        
        // 3. Кэш приложения
        File cacheDir = context.getCacheDir();
        File cacheFile = new File(cacheDir, fileName);
        if (canWriteToFile(cacheFile)) {
            return cacheFile;
        }
        
        return null;
    }
    
    private boolean canWriteToFile(File file) {
        try {
            // Проверяем, можем ли создать файл
            if (file.exists()) {
                file.delete();
            }
            return file.createNewFile();
        } catch (Exception e) {
            return false;
        }
    }
    
    private String getRecommendations(DatabaseHelper.TrainingStats stats) {
        StringBuilder rec = new StringBuilder();
        
        if (stats.getTotalTrainings() == 0) {
            rec.append("Начните первую тренировку! 💪\n");
            return rec.toString();
        }
        
        if (stats.getOverallAverage() < 8.0) {
            rec.append("• Уделите внимание базовой технике\n");
            rec.append("• Практикуйте дыхательные упражнения\n");
            rec.append("• Увеличьте количество тренировок\n");
        } else if (stats.getOverallAverage() < 9.0) {
            rec.append("• Хорошие результаты!\n");
            rec.append("• Работайте над стабильностью\n");
            rec.append("• Анализируйте ошибки\n");
        } else {
            rec.append("• Отличные результаты! 🏆\n");
            rec.append("• Продолжайте в том же духе\n");
        }
        
        rec.append("\nОбщие советы:\n");
        rec.append("• Тренируйтесь регулярно\n");
        rec.append("• Ведите дневник\n");
        rec.append("• Отдыхайте достаточно\n");
        
        return rec.toString();
    }
}
