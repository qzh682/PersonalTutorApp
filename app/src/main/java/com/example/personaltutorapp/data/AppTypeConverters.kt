package com.example.personaltutorapp.data

import androidx.room.TypeConverter
import com.example.personaltutorapp.model.PageType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppTypeConverters {

    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        try {
            val json = gson.toJson(list)
            println("Converting List<String> to JSON: $json")
            return json
        } catch (e: Exception) {
            println("Failed to convert List<String> to JSON: ${e.message}")
            throw e
        }
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        try {
            val listType = object : TypeToken<List<String>>() {}.type
            val result = gson.fromJson<List<String>>(data, listType)
            println("Converting JSON to List<String>: $data -> $result")
            return result
        } catch (e: Exception) {
            println("Failed to convert JSON to List<String>: ${e.message}")
            return emptyList()
        }
    }

    @TypeConverter
    fun fromPageType(type: PageType): String {
        val name = type.name
        println("Converting PageType to String: $type -> $name")
        return name
    }

    @TypeConverter
    fun toPageType(name: String): PageType {
        try {
            val type = PageType.valueOf(name)
            println("Converting String to PageType: $name -> $type")
            return type
        } catch (e: IllegalArgumentException) {
            println("Failed to convert String to PageType: $name, ${e.message}")
            throw e
        }
    }

    @TypeConverter
    fun fromDate(date: Date?): String? {
        try {
            val result = date?.let { dateFormat.format(it) }
            println("Converting Date to String: $date -> $result")
            return result
        } catch (e: Exception) {
            println("Failed to convert Date to String: ${e.message}")
            return null
        }
    }

    @TypeConverter
    fun toDate(dateString: String?): Date? {
        try {
            val result = dateString?.let { dateFormat.parse(it) }
            println("Converting String to Date: $dateString -> $result")
            return result
        } catch (e: Exception) {
            println("Failed to convert String to Date: ${e.message}")
            return null
        }
    }
}