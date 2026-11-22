package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timeHhmm: String, // "18:30"
    val daysMask: Int, // Битовая маска дней недели (Пн, Вт...)
    val enabled: Boolean = true,
    val title: String? = null,
    val templateId: Long? = null // Ссылка на шаблон, если есть
)