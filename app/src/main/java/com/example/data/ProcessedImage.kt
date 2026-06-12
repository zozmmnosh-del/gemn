package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_images")
data class ProcessedImage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageName: String,       // Custom Arabic/English title or date representation
    val originalPath: String?,   // Local path or identifier
    val cleanedPath: String,     // Local file system directory path
    val watermarkType: String,   // E.g., "Google Imagen", "SynthID", "Custom Brush"
    val timestamp: Long = System.currentTimeMillis()
)
