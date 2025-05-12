package com.example.personaltutorapp.data

import androidx.room.TypeConverter
import com.example.personaltutorapp.model.PageType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AppTypeConverters {

    private val gson = Gson()

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
}