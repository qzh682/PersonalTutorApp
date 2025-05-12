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
        require(options.size == 4) { "Options list must contain exactly 4 items" }
        require(options.all { it.isNotBlank() }) { "Options cannot be blank" }
        require(options.distinct().size == options.size) { "Options must be unique" }
        require(correctAnswerIndex in 0 until options.size) { "Correct answer index $correctAnswerIndex is out of bounds for options list of size ${options.size}" }
        require(quizId.isNotBlank()) { "Quiz ID cannot be blank" }
    }
}