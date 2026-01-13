package com.shooterpro.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrainingPlan {
    private static TrainingPlan instance;
    private Map<String, List<Exercise>> weeklyPlan;

    private TrainingPlan() {
        weeklyPlan = new HashMap<>();
        initializeExercises();
    }

    public static TrainingPlan getInstance() {
        if (instance == null) {
            instance = new TrainingPlan();
        }
        return instance;
    }

    private void initializeExercises() {
        // ПОНЕДЕЛЬНИК - Разминка и основы
        List<Exercise> monday = new ArrayList<>();
        monday.add(new Exercise("m1", "Разминка суставов", 
								"Круговые движения плечами, локтями, запястьями. 2 подхода по 15 повторений",
								"ПН", 10, "Разминка", "Начинающий"));
        monday.add(new Exercise("m2", "Дыхательная гимнастика", 
								"Глубокое дыхание 4-7-8: вдох 4 сек, задержка 7 сек, выдох 8 сек",
								"ПН", 5, "Разминка", "Начинающий"));
        monday.add(new Exercise("m3", "Удержание винтовки", 
								"Удержание в положении стоя 30 секунд, 5 подходов. Фокус на расслабление",
								"ПН", 15, "Техника", "Начинающий"));
        monday.add(new Exercise("m4", "Прицеливание без выстрела", 
								"Работа с прицелом на мишени без патронов. 20 повторений",
								"ПН", 20, "Техника", "Начинающий"));
        weeklyPlan.put("ПН", monday);

        // ВТОРНИК - Стабильность
        List<Exercise> tuesday = new ArrayList<>();
        tuesday.add(new Exercise("t1", "Планка на локтях", 
								 "Удержание планки 30-60 секунд, 3 подхода. Фокус на корпус",
								 "ВТ", 10, "Стабильность", "Начинающий"));
        tuesday.add(new Exercise("t2", "Баланс на одной ноге", 
								 "Стояние на одной ноге с закрытыми глазами. 30 секунд на каждую ногу",
								 "ВТ", 5, "Стабильность", "Начинающий"));
        tuesday.add(new Exercise("t3", "Спуск курка с весом", 
								 "Отработка спуска с утяжелением на крючке. 20 повторений",
								 "ВТ", 15, "Техника", "Продвинутый"));
        tuesday.add(new Exercise("t4", "Стрельба по точкам", 
								 "5 выстрелов по маленьким точкам на мишени. Анализ кучности",
								 "ВТ", 25, "Техника", "Продвинутый"));
        weeklyPlan.put("ВТ", tuesday);

        // СРЕДА - Силовая подготовка
        List<Exercise> wednesday = new ArrayList<>();
        wednesday.add(new Exercise("w1", "Отжимания", 
								   "3 подхода по 10-15 повторений. Медленно, с контролем",
								   "СР", 10, "Сила", "Начинающий"));
        wednesday.add(new Exercise("w2", "Приседания с паузой", 
								   "3 подхода по 15 повторений. Пауза 3 секунды в нижней точке",
								   "СР", 10, "Сила", "Начинающий"));
        wednesday.add(new Exercise("w3", "Тренировка с резиной", 
								   "Упражнения на растяжение резинового жгута для мышц спины",
								   "СР", 15, "Сила", "Продвинутый"));
        wednesday.add(new Exercise("w4", "Стрельба на время", 
								   "10 выстрелов за 5 минут. Фокус на скорость без потери качества",
								   "СР", 30, "Техника", "Профи"));
        weeklyPlan.put("СР", wednesday);

        // ЧЕТВЕРГ - Техника дыхания
        List<Exercise> thursday = new ArrayList<>();
        thursday.add(new Exercise("th1", "Дыхание животом", 
								  "Лежа на спине, дыхание только животом. 5 минут",
								  "ЧТ", 5, "Разминка", "Начинающий"));
        thursday.add(new Exercise("th2", "Задержка дыхания", 
								  "Задержка на 15-20 секунд после выдоха. 10 повторений",
								  "ЧТ", 10, "Техника", "Продвинутый"));
        thursday.add(new Exercise("th3", "Стрельба на выдохе", 
								  "Отработка выстрела в момент естественной паузы дыхания",
								  "ЧТ", 20, "Техника", "Продвинутый"));
        thursday.add(new Exercise("th4", "Тренировка в темноте", 
								  "Закрыть глаза, принять положение, открыть - проверить ровность",
								  "ЧТ", 15, "Техника", "Профи"));
        weeklyPlan.put("ЧТ", thursday);

        // ПЯТНИЦА - Комплексная тренировка
        List<Exercise> friday = new ArrayList<>();
        friday.add(new Exercise("f1", "Быстрая разминка", 
								"5 минут динамической растяжки всех групп мышц",
								"ПТ", 5, "Разминка", "Начинающий"));
        friday.add(new Exercise("f2", "Серия из 20 выстрелов", 
								"20 выстрелов с полным циклом: подготовка, прицеливание, спуск",
								"ПТ", 40, "Техника", "Продвинутый"));
        friday.add(new Exercise("f3", "Анализ группировки", 
								"После каждой серии анализ кучности, запись результатов",
								"ПТ", 15, "Техника", "Продвинутый"));
        friday.add(new Exercise("f4", "Медитация после стрельбы", 
								"5 минут расслабления, анализ ощущений, планирование улучшений",
								"ПТ", 5, "Завершение", "Начинающий"));
        weeklyPlan.put("ПТ", friday);

        // СУББОТА - Легкая активность
        List<Exercise> saturday = new ArrayList<>();
        saturday.add(new Exercise("s1", "Прогулка на свежем воздухе", 
								  "30 минут спокойной ходьбы для восстановления",
								  "СБ", 30, "Восстановление", "Начинающий"));
        saturday.add(new Exercise("s2", "Растяжка всего тела", 
								  "Статическая растяжка всех основных мышечных групп",
								  "СБ", 15, "Восстановление", "Начинающий"));
        saturday.add(new Exercise("s3", "Визуализация выстрела", 
								  "Ментальная тренировка: представление идеального выстрела",
								  "СБ", 10, "Ментальная", "Продвинутый"));
        weeklyPlan.put("СБ", saturday);

        // ВОСКРЕСЕНЬЕ - Отдых и планирование
        List<Exercise> sunday = new ArrayList<>();
        sunday.add(new Exercise("su1", "Полный отдых", 
								"День без физической активности, восстановление организма",
								"ВС", 0, "Отдых", "Начинающий"));
        sunday.add(new Exercise("su2", "Планирование недели", 
								"Анализ прошедшей недели, постановка целей на следующую",
								"ВС", 15, "Планирование", "Начинающий"));
        sunday.add(new Exercise("su3", "Чтение литературы", 
								"Изучение техник стрельбы, биомеханики, психологии",
								"ВС", 30, "Обучение", "Продвинутый"));
        weeklyPlan.put("ВС", sunday);
    }

    public List<Exercise> getExercisesForDay(String day) {
        List<Exercise> exercises = weeklyPlan.get(day);
		return exercises != null ? exercises : new ArrayList<Exercise>();
	}

    public List<String> getAllDays() {
        return new ArrayList<>(weeklyPlan.keySet());
    }

    public int getTotalDurationForDay(String day) {
        int total = 0;
        List<Exercise> exercises = weeklyPlan.get(day);
        if (exercises != null) {
            for (Exercise ex : exercises) {
                total += ex.getDuration();
            }
        }
        return total;
    }
}
