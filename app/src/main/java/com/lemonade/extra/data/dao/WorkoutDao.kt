package com.lemonade.extra.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lemonade.extra.data.entity.WorkoutEntity
import com.lemonade.extra.data.entity.WorkoutExerciseEntity
import com.lemonade.extra.data.entity.WorkoutSetEntity

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workouts WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertWorkoutSet(workoutSet: WorkoutSetEntity): Long

    // Обновляем статус тренировки на "Завершена"
    @Query("UPDATE workouts SET status = 'FINISHED' WHERE id = :workoutId")
    suspend fun finishWorkout(workoutId: Long)
}