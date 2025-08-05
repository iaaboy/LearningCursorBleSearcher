package com.example.myandroidapp

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

data class SavedFile(
    val file: File,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val type: String
) {
    val formattedSize: String
        get() = when {
            size < 1024 -> "${size} B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
    
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(lastModified))
    
    val displayName: String
        get() = name.removeSuffix(".csv").removeSuffix(".json").removeSuffix(".txt")
}

class SavedFileManager(private val context: Context) {
    
    fun getSavedFiles(): List<SavedFile> {
        val filesDir = context.filesDir
        val files = filesDir.listFiles()?.filter { file ->
            file.isFile && (file.name.endsWith(".csv") || 
                          file.name.endsWith(".json") || 
                          file.name.endsWith(".txt"))
        } ?: emptyList()
        
        return files.map { file ->
            SavedFile(
                file = file,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                type = when {
                    file.name.endsWith(".csv") -> "CSV"
                    file.name.endsWith(".json") -> "JSON"
                    file.name.endsWith(".txt") -> "TXT"
                    else -> "Unknown"
                }
            )
        }.sortedByDescending { it.lastModified }
    }
    
    fun deleteFile(file: SavedFile): Boolean {
        return try {
            file.file.delete()
        } catch (e: Exception) {
            false
        }
    }
    
    fun getFileContent(file: SavedFile): String? {
        return try {
            file.file.readText()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getFileCount(): Int = getSavedFiles().size
    
    fun getTotalSize(): Long = getSavedFiles().sumOf { it.size }
} 