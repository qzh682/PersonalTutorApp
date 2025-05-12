package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.personaltutorapp.data.AppTypeConverters

@Entity(tableName = "quiz_questions")
@TypeConverters(AppTypeConverters::class)
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val quizId: String
) {
    init {
        require(id.isNotBlank()) { "Question ID cannot be blank" }
        require(question.isNotBlank()) { "Question cannot be blank" }
        // Allow 2 options for true/false questions, 4 options for multiple-choice
        require(options.size == 2 || options.size == 4) { "Options list must contain exactly 2 or 4 items, but found ${options.size}" }
        require(options.all { it.isNotBlank() }) { "Options cannot be blank" }
        require(options.distinct().size == options.size) { "Options must be unique" }
        require(correctAnswerIndex in 0 until options.size) { "Correct answer index $correctAnswerIndex is out of bounds for options list of size ${options.size}" }
        require(quizId.isNotBlank()) { "Quiz ID cannot be blank" }
    }
}