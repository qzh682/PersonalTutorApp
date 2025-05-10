package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val quizId: String
)
