package com.shooterpro.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String TAG = "ReminderReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Получено напоминание!");

        String title = intent.getStringExtra("title");
        String message = intent.getStringExtra("message");

        SharedPreferences prefs = context.getSharedPreferences("ShooterPRO", Context.MODE_PRIVATE);
        boolean soundEnabled = prefs.getBoolean("reminder_sound", true);
        boolean vibrationEnabled = prefs.getBoolean("reminder_vibration", true);
        int soundIndex = prefs.getInt("reminder_sound_index", 0);

        if (title == null) title = "⏰ Время тренировки!";
        if (message == null) message = "Не забудьте про сегодняшнюю тренировку!";

        Log.d(TAG, "Показываем уведомление: " + title);
        showNotification(context, title, message, soundEnabled, vibrationEnabled, soundIndex);
    }

    private void showNotification(Context context, String title, String message, 
								  boolean soundEnabled, boolean vibrationEnabled, int soundIndex) {
        NotificationManager notificationManager = 
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager == null) {
            Log.e(TAG, "NotificationManager не доступен!");
            return;
        }

        // Intent для открытия приложения
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent;
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        pendingIntent = PendingIntent.getActivity(context, 0, appIntent, flags);

        // Настройка звука
        Uri soundUri = null;
        if (soundEnabled) {
            // Выбираем звук в зависимости от выбора пользователя
            switch (soundIndex) {
                case 0: // Обычный сигнал
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    break;
                case 1: // Длинный сигнал
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    break;
                case 2: // Короткий сигнал (системный звук, если beep.ogg нет)
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    break;
                case 3: // Тревога (системный звук, если alarm.ogg нет)
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    break;
                default:
                    soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            }
        }

        // Создаем уведомление
        Notification.Builder builder = new Notification.Builder(context);

        // Для старых версий Android настраиваем звук и вибрацию
        int defaults = 0;
        if (soundEnabled) {
            defaults |= Notification.DEFAULT_SOUND;
            if (soundUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                builder.setSound(soundUri);
            }
        }
        if (vibrationEnabled) {
            defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (defaults != 0) {
            builder.setDefaults(defaults);
        }

        builder.setSmallIcon(android.R.drawable.ic_dialog_info)
			.setContentTitle(title)
			.setContentText(message)
			.setContentIntent(pendingIntent)
			.setAutoCancel(true)
			.setPriority(Notification.PRIORITY_HIGH);

        // Для Android 4.1+ добавляем большой текст
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Notification.BigTextStyle bigTextStyle = new Notification.BigTextStyle();
            bigTextStyle.bigText(message);
            bigTextStyle.setBigContentTitle(title);
            builder.setStyle(bigTextStyle);
        }

        int notificationId = (int) System.currentTimeMillis();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationManager.notify(notificationId, builder.build());
        } else {
            notificationManager.notify(notificationId, builder.getNotification());
        }

        Log.d(TAG, "Уведомление показано с ID: " + notificationId);
    }

    // Метод для тестирования напоминаний
    public static void testReminder(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction("com.shooterpro.app.REMINDER_ACTION");
        intent.putExtra("title", "⏰ ТЕСТ Напоминание!");
        intent.putExtra("message", "Это тестовое напоминание для проверки работы.");

        context.sendBroadcast(intent);
        Log.d(TAG, "Тестовое напоминание отправлено");
    }
}
