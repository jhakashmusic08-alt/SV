package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentActivityDao {
    @Query("SELECT * FROM recent_activities ORDER BY timestamp DESC LIMIT 20")
    fun getRecentActivities(): Flow<List<RecentActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: RecentActivity): Long

    @Delete
    suspend fun deleteActivity(activity: RecentActivity)

    @Query("DELETE FROM recent_activities")
    suspend fun clearAllActivities()
}
