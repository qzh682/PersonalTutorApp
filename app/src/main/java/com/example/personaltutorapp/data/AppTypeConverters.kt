package com.example.personaltutorapp.data

import androidx.room.TypeConverter
import com.example.personaltutorapp.model.LessonPage
import com.example.personaltutorapp.model.PageType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class AppTypeConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromLessonPageList(pages: List<LessonPage>): String {
        return gson.toJson(pages)
    }

    @TypeConverter
    fun toLessonPageList(data: String): List<LessonPage> {
        val listType = object : TypeToken<List<LessonPage>>() {}.type
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun fromPageType(type: PageType): String {
        return type.name
    }

    @TypeConverter
    fun toPageType(name: String): PageType {
        return PageType.valueOf(name)
    }
}
