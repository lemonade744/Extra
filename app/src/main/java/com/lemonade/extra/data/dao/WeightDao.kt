package com.lemonade.extra.data.dao

import androidx.room.Dao
import androidx.room.Insert
import com.lemonade.extra.data.entity.WeightHistoryEntity

@Dao
interface WeightDao {
    @Insert
    suspend fun insertWeight(weight: WeightHistoryEntity)
}