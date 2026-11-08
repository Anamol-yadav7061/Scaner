package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_logs")
data class ScanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val rawContent: String,
    val format: String,
    val parsedType: String, // URL, WIFI, UPI, VCARD, EMAIL, PHONE, SMS, GEO, TEXT
    val securityRating: String, // SAFE, WARNING, DANGER
    val timestamp: Long = System.currentTimeMillis(),
    val isBookmarked: Boolean = false,
    val title: String = "",
    val metadataJson: String = ""
)
