package com.example.data.repository

import com.example.data.dao.ScanItemDao
import com.example.data.model.ScanItem
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanRepository(private val dao: ScanItemDao) {

    val allScans: Flow<List<ScanItem>> = dao.getAll()
    val bookmarkedScans: Flow<List<ScanItem>> = dao.getBookmarked()

    fun getScanById(id: Long): Flow<ScanItem?> = dao.getById(id)

    fun searchScans(query: String): Flow<List<ScanItem>> = dao.search(query)

    suspend fun recordScan(
        rawContent: String,
        format: String,
        parsedType: String,
        securityRating: String,
        title: String = "",
        metadataJson: String = ""
    ): Long {
        val item = ScanItem(
            rawContent = rawContent,
            format = format,
            parsedType = parsedType,
            securityRating = securityRating,
            title = title.ifBlank { rawContent.take(40) },
            metadataJson = metadataJson
        )
        return dao.insert(item)
    }

    suspend fun toggleBookmark(item: ScanItem) {
        dao.update(item.copy(isBookmarked = !item.isBookmarked))
    }

    suspend fun delete(item: ScanItem) {
        dao.delete(item)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }

    fun exportToJson(items: List<ScanItem>): String {
        val root = JSONObject()
        root.put("app", "Anamol QR Code Scanner")
        root.put("version", "2.0")
        root.put("exportTime", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US).format(Date()))
        val array = JSONArray()
        for (item in items) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("rawContent", item.rawContent)
            obj.put("format", item.format)
            obj.put("parsedType", item.parsedType)
            obj.put("securityRating", item.securityRating)
            obj.put("timestamp", item.timestamp)
            obj.put("isBookmarked", item.isBookmarked)
            obj.put("title", item.title)
            array.put(obj)
        }
        root.put("records", array)
        return root.toString(2)
    }

    fun exportToCsv(items: List<ScanItem>): String {
        val sb = StringBuilder()
        sb.append("ID,Timestamp,Format,Type,Security,Bookmarked,Title,RawContent\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (item in items) {
            val dateStr = dateFormat.format(Date(item.timestamp))
            val escapedRaw = "\"" + item.rawContent.replace("\"", "\"\"") + "\""
            val escapedTitle = "\"" + item.title.replace("\"", "\"\"") + "\""
            sb.append("${item.id},$dateStr,${item.format},${item.parsedType},${item.securityRating},${item.isBookmarked},$escapedTitle,$escapedRaw\n")
        }
        return sb.toString()
    }
}
