package com.example.personaltutorapp.model

data class Quiz(
    val id: String,
    val courseId: String,
    val questions: List<QuizQuestion> = emptyList(),
    val submissions: List<QuizSubmission> = emptyList(),
    val isPublished: Boolean = false // Added isPublished field
) {
    init {
        require(id.isNotBlank()) { "Quiz ID cannot be blank" }
        require(courseId.isNotBlank()) { "Course ID cannot be blank" }
    }
}