package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val date: String, // Формат: "2026-11-22"
    val status: String = "ACTIVE" // ACTIVE, FINISHED, CANCELLED
)