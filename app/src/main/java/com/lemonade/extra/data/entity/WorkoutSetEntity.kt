package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutExerciseId: Long,
    val setIndex: Int,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val durationSec: Int? = null,
    val distanceM: Double? = null,
    val restSec: Int? = null,
    val completed: Boolean = false
)