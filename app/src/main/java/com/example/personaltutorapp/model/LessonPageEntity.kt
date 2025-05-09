package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.personaltutorapp.model.PageType


@Entity(tableName = "lesson_pages")
data class LessonPageEntity(
    @PrimaryKey val id: String,
    val lessonId: String, // 外键，指向 LessonEntity.id
    val type: PageType,
    val content: String
)


