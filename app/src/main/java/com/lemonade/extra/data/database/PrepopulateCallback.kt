package com.lemonade.extra.data.database // Твой пакет!

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class PrepopulateCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)

        // 1. Создаем дефолтного пользователя
        db.execSQL("INSERT INTO users (id, weight_kg, height_cm, default_rest_sec) VALUES (1, 75.0, 180.0, 90)")

        // 2. Базовые группы мышц
        db.execSQL("INSERT INTO muscle_groups (code, name) VALUES ('legs', 'Ноги')")
        db.execSQL("INSERT INTO muscle_groups (code, name) VALUES ('abs', 'Пресс')")
        db.execSQL("INSERT INTO muscle_groups (code, name) VALUES ('back', 'Спина')")
        db.execSQL("INSERT INTO muscle_groups (code, name) VALUES ('chest', 'Грудь')")
        db.execSQL("INSERT INTO muscle_groups (code, name) VALUES ('arms', 'Руки')")
        db.execSQL("INSERT INTO muscle_groups (code, name) VALUES ('cardio', 'Кардио')")

        // 3. Базовые упражнения
        db.execSQL("INSERT INTO exercises (name, type, isCustom) VALUES ('Приседания', 'STRENGTH', 0)")
        db.execSQL("INSERT INTO exercises (name, type, isCustom) VALUES ('Отжимания', 'BODYWEIGHT', 0)")
        db.execSQL("INSERT INTO exercises (name, type, isCustom) VALUES ('Подтягивания', 'BODYWEIGHT', 0)")
        db.execSQL("INSERT INTO exercises (name, type, isCustom) VALUES ('Планка', 'TIMED', 0)")
        db.execSQL("INSERT INTO exercises (name, type, isCustom) VALUES ('Бег', 'RUNNING', 0)")
    }
}