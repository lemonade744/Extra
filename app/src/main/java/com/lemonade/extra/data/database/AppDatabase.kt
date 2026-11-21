package com.lemonade.extra.data.database // <-- Твой пакет!

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lemonade.extra.data.dao.ExerciseDao
import com.lemonade.extra.data.dao.MuscleGroupDao
import com.lemonade.extra.data.dao.UserDao
import com.lemonade.extra.data.dao.WorkoutDao
import com.lemonade.extra.data.entity.ExerciseEntity
import com.lemonade.extra.data.entity.MuscleGroupEntity
import com.lemonade.extra.data.entity.UserEntity
import com.lemonade.extra.data.entity.WorkoutEntity
import com.lemonade.extra.data.entity.WorkoutExerciseEntity
import com.lemonade.extra.data.entity.WorkoutSetEntity

@Database(
    entities = [
        UserEntity::class,
        MuscleGroupEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class, // <-- Добавили
        WorkoutSetEntity::class       // <-- Добавили
    ],
    version = 3, // <-- Поменяли версию
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    // Подключаем наши DAO
    abstract fun userDao(): UserDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Синглтон для получения доступа к базе из любого места приложения
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration() // <-- Добавили (при смене версии БД просто пересоздастся)
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}