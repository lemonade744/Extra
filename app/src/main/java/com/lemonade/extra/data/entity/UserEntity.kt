package com.lemonade.extra.data.entity // У тебя тут будет твой package name!

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1, // У нас будет только один пользователь в этой таблице

    @ColumnInfo(name = "weight_kg")
    val weightKg: Double,

    @ColumnInfo(name = "height_cm")
    val heightCm: Double,

    @ColumnInfo(name = "default_rest_sec")
    val defaultRestSec: Int = 90
)