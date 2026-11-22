package com.lemonade.extra.data.dao

data class SetWithType(
    val workoutId: Long,
    val type: String,
    val reps: Int?,
    val weightKg: Double?,
    val durationSec: Int?
)