package com.shooterpro.app.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String[] MONTHS_RU = {
        "января", "февраля", "марта", "апреля", "мая", "июня",
        "июля", "августа", "сентября", "октября", "ноября", "декабря"
    };

    private static final String[] WEEKDAYS_RU = {
        "Воскресенье", "Понедельник", "Вторник", "Среда", 
        "Четверг", "Пятница", "Суббота"
    };

    private static final String[] WEEKDAYS_SHORT = {
        "ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ"
    };

    public static String formatDate(int day, int month, int year) {
        return String.format(Locale.getDefault(), "%02d.%02d.%04d", day, month + 1, year);
    }

    public static String getDateText(int day, int month) {
        return day + " " + MONTHS_RU[month];
    }

    public static String getWeekday(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return WEEKDAYS_RU[dayOfWeek];
    }

    public static String getWeekdayShort(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        return WEEKDAYS_SHORT[dayOfWeek];
    }

    public static boolean isToday(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);

        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
			today.get(Calendar.MONTH) == date.get(Calendar.MONTH) &&
			today.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isPast(int year, int month, int day) {
        Calendar today = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.set(year, month, day);

        return date.before(today) && !isToday(year, month, day);
    }

    public static boolean isSunday(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    public static boolean isFriday(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        return cal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY;
    }
}
