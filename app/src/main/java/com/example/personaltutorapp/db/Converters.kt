package com.example.personaltutorapp.db

import androidx.room.TypeConverter
import com.example.personaltutorapp.model.LessonPage
import com.example.personaltutorapp.model.PageType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromPageType(value: PageType): String {
        return value.name
    }

    @TypeConverter
    fun toPageType(value: String): PageType {
        return PageType.valueOf(value)
    }

    @TypeConverter
    fun fromLessonPageList(pages: List<LessonPage>): String {
        return gson.toJson(pages)
    }

    @TypeConverter
    fun toLessonPageList(json: String): List<LessonPage> {
        val type = object : TypeToken<List<LessonPage>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }
}
