package com.lemonade.extra.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.lemonade.extra.data.dao.*
import com.lemonade.extra.data.entity.*

@Database(
    entities = [
        UserEntity::class,
        MuscleGroupEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        WeightHistoryEntity::class,
        ReminderEntity::class,
        WorkoutMuscleGroupEntity::class,
        TemplateEntity::class,
        TemplateExerciseEntity::class,
        TemplateMuscleGroupEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun muscleGroupDao(): MuscleGroupDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun weightDao(): WeightDao
    abstract fun reminderDao(): ReminderDao
    abstract fun templateDao(): TemplateDao

    // Пока мы не написали для них методы, просто объявим,
    // чтобы Room знал, что они существуют. (Создадим их позже, когда дойдем до UI).
    // Если они красные, закомментируй их до следующего шага.
    // Но лучше создать пустые интерфейсы (см. ниже).

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "workout_database"
                )
                    .fallbackToDestructiveMigration() // Сносим старую БД при смене версии
                    .addCallback(PrepopulateCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}