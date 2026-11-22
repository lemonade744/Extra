package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_history")
data class WeightHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String, // "2026-11-22"
    val weightKg: Double
)