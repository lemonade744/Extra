package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "workout_muscle_groups",
    primaryKeys = ["workoutId", "muscleGroupId"],
    foreignKeys = [
        ForeignKey(entity = WorkoutEntity::class, parentColumns = ["id"], childColumns = ["workoutId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MuscleGroupEntity::class, parentColumns = ["id"], childColumns = ["muscleGroupId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class WorkoutMuscleGroupEntity(
    val workoutId: Long,
    val muscleGroupId: Long
)