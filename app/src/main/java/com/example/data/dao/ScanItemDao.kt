package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanItemDao {
    @Query("SELECT * FROM scan_logs ORDER BY timestamp DESC")
    fun getAll(): Flow<List<ScanItem>>

    @Query("SELECT * FROM scan_logs WHERE isBookmarked = 1 ORDER BY timestamp DESC")
    fun getBookmarked(): Flow<List<ScanItem>>

    @Query("SELECT * FROM scan_logs WHERE id = :id")
    fun getById(id: Long): Flow<ScanItem?>

    @Query("SELECT * FROM scan_logs WHERE rawContent LIKE '%' || :query || '%' OR title LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun search(query: String): Flow<List<ScanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScanItem): Long

    @Update
    suspend fun update(item: ScanItem)

    @Delete
    suspend fun delete(item: ScanItem)

    @Query("DELETE FROM scan_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scan_logs")
    suspend fun clearAll()
}
