package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.personaltutorapp.data.AppTypeConverters

@Entity(tableName = "quiz_questions")
@TypeConverters(AppTypeConverters::class)
data class QuizQuestionEntity(
    @PrimaryKey val id: String,
    val quizId: String,
    val question: String,
    val options: String, // Room stores this as a JSON string, converted to List<String> via AppTypeConverters
    val correctAnswerIndex: Int
) {
    init {
        require(id.isNotBlank()) { "Question ID cannot be blank" }
        require(quizId.isNotBlank()) { "Quiz ID cannot be blank" }
        require(question.isNotBlank()) { "Question cannot be blank" }
    }
}