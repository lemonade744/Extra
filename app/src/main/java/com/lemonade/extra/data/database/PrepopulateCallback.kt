package com.lemonade.extra.data.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class PrepopulateCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        db.execSQL("INSERT INTO users (id, weight_kg, height_cm, default_rest_sec) VALUES (1, 75.0, 180.0, 90)")

        // 1. Создаем группы мышц (с явными ID для связи)
        db.execSQL("INSERT INTO muscle_groups (id, code, name) VALUES (1, 'legs', 'Ноги')")
        db.execSQL("INSERT INTO muscle_groups (id, code, name) VALUES (2, 'abs', 'Пресс')")
        db.execSQL("INSERT INTO muscle_groups (id, code, name) VALUES (3, 'back', 'Спина')")
        db.execSQL("INSERT INTO muscle_groups (id, code, name) VALUES (4, 'chest', 'Грудь')")
        db.execSQL("INSERT INTO muscle_groups (id, code, name) VALUES (5, 'arms', 'Руки')")
        db.execSQL("INSERT INTO muscle_groups (id, code, name) VALUES (6, 'cardio', 'Кардио')")

        // 2. Создаем упражнения и привязываем к группам
        db.execSQL("INSERT INTO exercises (name, type, primary_muscle_group_id, isCustom) VALUES ('Приседания', 'STRENGTH', 1, 0)")
        db.execSQL("INSERT INTO exercises (name, type, primary_muscle_group_id, isCustom) VALUES ('Отжимания', 'BODYWEIGHT', 4, 0)")
        db.execSQL("INSERT INTO exercises (name, type, primary_muscle_group_id, isCustom) VALUES ('Подтягивания', 'BODYWEIGHT', 3, 0)")
        db.execSQL("INSERT INTO exercises (name, type, primary_muscle_group_id, isCustom) VALUES ('Планка', 'TIMED', 2, 0)")
        db.execSQL("INSERT INTO exercises (name, type, primary_muscle_group_id, isCustom) VALUES ('Бег', 'RUNNING', 6, 0)")
    }
}