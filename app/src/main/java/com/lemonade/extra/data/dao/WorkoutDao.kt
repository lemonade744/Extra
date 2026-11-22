package com.lemonade.extra.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.lemonade.extra.data.entity.WorkoutEntity
import com.lemonade.extra.data.entity.WorkoutExerciseEntity
import com.lemonade.extra.data.entity.WorkoutSetEntity

@Dao
interface WorkoutDao {
    @Insert
    suspend fun insertWorkout(workout: WorkoutEntity): Long

    @Query("SELECT * FROM workouts WHERE status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveWorkout(): WorkoutEntity?

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Query("""
        SELECT SUM((COALESCE(s.reps, 0) + COALESCE(s.durationSec, 0)) * COALESCE(s.weightKg, 0))
        FROM workout_sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        WHERE we.workoutId = :workoutId AND s.completed = 1
    """)
    suspend fun getWorkoutVolume(workoutId: Long): Double?

    @Query("""
        SELECT we.workoutId, e.type, s.reps, s.weightKg, s.durationSec
        FROM workout_sets s
        JOIN workout_exercises we ON s.workoutExerciseId = we.id
        JOIN exercises e ON we.exerciseId = e.id
        WHERE s.completed = 1
    """)
    suspend fun getAllSetsWithType(): List<SetWithType>

    @Query("SELECT * FROM workout_sets")
    suspend fun getAllSets(): List<WorkoutSetEntity>

    @Insert
    suspend fun insertWorkoutExercise(workoutExercise: WorkoutExerciseEntity): Long

    @Insert
    suspend fun insertWorkoutSet(workoutSet: WorkoutSetEntity): Long

    @Query("UPDATE workouts SET status = 'FINISHED' WHERE id = :workoutId")
    suspend fun finishWorkout(workoutId: Long)

    @Query("SELECT * FROM workouts WHERE status = 'FINISHED' ORDER BY date DESC")
    suspend fun getFinishedWorkouts(): List<WorkoutEntity>
}