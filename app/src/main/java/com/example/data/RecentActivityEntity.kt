package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_activities")
data class RecentActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // UPLOAD_COMPLETE, METADATA_UPDATE, PROFILE_CHANGE
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)
