package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ColumnInfo


@Entity(
    tableName = "exercises",
    foreignKeys = [
        ForeignKey(
            entity = MuscleGroupEntity::class,
            parentColumns = ["id"],
            childColumns = ["primary_muscle_group_id"], // <-- ИСПРАВЛЕНО ЗДЕСЬ
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    val type: String,

    @ColumnInfo(name = "primary_muscle_group_id")
    val primaryMuscleGroupId: Long? = null,

    val description: String? = null,

    val isCustom: Boolean = false
)