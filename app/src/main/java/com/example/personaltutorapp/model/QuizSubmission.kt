package com.example.personaltutorapp.model

/**
 * Represents a single student's submission for a quiz.
 */
data class QuizSubmission(
    val userId: String,
    val score: Int
)
