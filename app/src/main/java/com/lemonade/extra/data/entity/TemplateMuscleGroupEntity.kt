package com.lemonade.extra.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "template_muscle_groups",
    primaryKeys = ["templateId", "muscleGroupId"],
    foreignKeys = [
        ForeignKey(entity = TemplateEntity::class, parentColumns = ["id"], childColumns = ["templateId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = MuscleGroupEntity::class, parentColumns = ["id"], childColumns = ["muscleGroupId"], onDelete = ForeignKey.CASCADE)
    ]
)
data class TemplateMuscleGroupEntity(
    val templateId: Long,
    val muscleGroupId: Long
)