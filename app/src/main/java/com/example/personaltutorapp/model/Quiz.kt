package com.example.personaltutorapp.model

data class Quiz(
    val id: String,
    val courseId: String,
    val questions: List<QuizQuestion>,
    val submissions: List<QuizSubmission> = emptyList()
)

