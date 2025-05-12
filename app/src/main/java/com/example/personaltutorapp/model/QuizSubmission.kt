package com.example.personaltutorapp.model

/**
 * Represents a single student's submission for a quiz.
 */
data class QuizSubmission(
    val userId: String,
    val score: Int
) {
    init {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
        require(score >= 0) { "Score cannot be negative" }
    }
}