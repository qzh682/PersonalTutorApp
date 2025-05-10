package com.example.personaltutorapp.db

import androidx.room.TypeConverter
import com.example.personaltutorapp.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromPageType(value: PageType): String = value.name

    @TypeConverter
    fun toPageType(value: String): PageType = PageType.valueOf(value)

    @TypeConverter
    fun fromLessonPageList(pages: List<LessonPage>): String = gson.toJson(pages)

    @TypeConverter
    fun toLessonPageList(json: String): List<LessonPage> {
        val type = object : TypeToken<List<LessonPage>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String = gson.toJson(list)

    @TypeConverter
    fun toStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromQuizQuestionList(questions: List<QuizQuestion>): String = gson.toJson(questions)

    @TypeConverter
    fun toQuizQuestionList(json: String): List<QuizQuestion> {
        val type = object : TypeToken<List<QuizQuestion>>() {}.type
        return gson.fromJson(json, type)
    }

    @TypeConverter
    fun fromQuizSubmissionList(list: List<QuizSubmission>): String = gson.toJson(list)

    @TypeConverter
    fun toQuizSubmissionList(json: String): List<QuizSubmission> {
        val type = object : TypeToken<List<QuizSubmission>>() {}.type
        return gson.fromJson(json, type)
    }
}
