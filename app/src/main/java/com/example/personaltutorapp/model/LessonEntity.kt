package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.personaltutorapp.data.AppTypeConverters

@Entity(tableName = "lessons")
@TypeConverters(AppTypeConverters::class)
data class LessonEntity(
    @PrimaryKey val id: String,
    val courseId: String, // 外键关系，指向 CourseEntity.id
    val title: String,
    val completedByUserIds: List<String> // 使用 TypeConverter 存储
) {
    init {
        require(id.isNotBlank()) { "Lesson ID cannot be blank" }
        require(courseId.isNotBlank()) { "Course ID cannot be blank" }
        require(title.isNotBlank()) { "Lesson title cannot be blank" }
    }
}