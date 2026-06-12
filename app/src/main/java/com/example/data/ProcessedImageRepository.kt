package com.example.data

import kotlinx.coroutines.flow.Flow

class ProcessedImageRepository(private val dao: ProcessedImageDao) {
    val allImages: Flow<List<ProcessedImage>> = dao.getAllProcessedImages()

    suspend fun insert(image: ProcessedImage) {
        dao.insertProcessedImage(image)
    }

    suspend fun delete(image: ProcessedImage) {
        dao.deleteProcessedImage(image)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearHistory()
    }
}
