package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val status: String = "ACTIVE",
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val notes: String? = null,
    val difficulty: Int? = null,
    val totalVolume: Double? = null,
    val totalDurationSec: Int? = null
)