package com.lemonade.extra.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lemonade.extra.data.entity.MuscleGroupEntity

@Dao
interface MuscleGroupDao {
    @Query("SELECT * FROM muscle_groups")
    suspend fun getAllGroups(): List<MuscleGroupEntity>

    @Query("SELECT * FROM muscle_groups")
    suspend fun getAllGroupsList(): List<MuscleGroupEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGroups(groups: List<MuscleGroupEntity>)
}