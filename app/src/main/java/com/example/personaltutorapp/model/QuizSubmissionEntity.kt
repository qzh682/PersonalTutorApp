package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_submissions")
data class QuizSubmissionEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val userId: String,
    val score: Int
) {
    init {
        require(id.isNotBlank()) { "Submission ID cannot be blank" }
        require(courseId.isNotBlank()) { "Course ID cannot be blank" }
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(score >= 0) { "Score cannot be negative" }
    }
}