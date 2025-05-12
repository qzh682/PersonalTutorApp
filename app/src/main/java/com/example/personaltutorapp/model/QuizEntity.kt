package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val isPublished: Boolean = false // Added isPublished field
) {
    init {
        require(id.isNotBlank()) { "Quiz ID cannot be blank" }
        require(courseId.isNotBlank()) { "Course ID cannot be blank" }
    }
}