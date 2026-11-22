package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String, // Например, "День ног"
    val description: String? = null,
    val isCustom: Boolean = false // true - создан пользователем, false - базовый
)