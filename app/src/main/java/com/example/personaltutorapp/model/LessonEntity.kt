package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String, // 外键关系，指向 CourseEntity.id
    val title: String,
    val pages: List<LessonPage>, // 需使用 TypeConverter
    val completedByUserIds: List<String> // 同样需 TypeConverter
)
