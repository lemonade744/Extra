package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "muscle_groups")
data class MuscleGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // 0 означает, что база сама придумает ID при вставке

    val code: String, // 'legs', 'chest', 'back'

    val name: String  // 'Ноги', 'Грудь', 'Спина'
)