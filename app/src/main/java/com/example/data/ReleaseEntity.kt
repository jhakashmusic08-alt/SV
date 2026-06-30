package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "releases")
data class Release(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val artistName: String,
    val featuringArtist: String = "",
    val lyricist: String = "",
    val composer: String = "",
    val genre: String,
    val language: String,
    val releaseDate: String,
    val recordLabel: String = "ST Digital Free Records",
    val upcCode: String = "",
    val isrcCode: String = "",
    val status: String = "Pending Review", // Pending Review, Approved, Delivered, Live
    val audioFileName: String = "",
    val artworkResourceName: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
