package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProcessedImageDao {
    @Query("SELECT * FROM processed_images ORDER BY timestamp DESC")
    fun getAllProcessedImages(): Flow<List<ProcessedImage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProcessedImage(image: ProcessedImage)

    @Delete
    suspend fun deleteProcessedImage(image: ProcessedImage)

    @Query("DELETE FROM processed_images WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM processed_images")
    suspend fun clearHistory()
}
