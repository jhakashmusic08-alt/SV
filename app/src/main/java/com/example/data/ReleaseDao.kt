package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReleaseDao {
    @Query("SELECT * FROM releases ORDER BY createdAt DESC")
    fun getAllReleases(): Flow<List<Release>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRelease(release: Release): Long

    @Delete
    suspend fun deleteRelease(release: Release)

    @Query("UPDATE releases SET status = :status WHERE id = :id")
    suspend fun updateReleaseStatus(id: Int, status: String)

    @Query("SELECT * FROM releases WHERE id = :id LIMIT 1")
    suspend fun getReleaseById(id: Int): Release?
}
