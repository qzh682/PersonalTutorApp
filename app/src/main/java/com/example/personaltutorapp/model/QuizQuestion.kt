package com.example.personaltutorapp.model

data class QuizQuestion(
    val id: String,
    val question: String,
    val options: List<String>,
    val correctAnswerIndex: Int
) {
    init {
        require(id.isNotBlank()) { "Question ID cannot be blank" }
        require(question.isNotBlank()) { "Question cannot be blank" }
        // Allow 2 options for true/false questions, 4 options for multiple-choice
        require(options.size == 2 || options.size == 4) { "Options list must contain exactly 2 or 4 items, but found ${options.size}" }
        require(options.all { it.isNotBlank() }) { "Options cannot be blank" }
        require(options.distinct().size == options.size) { "Options must be unique" }
        require(correctAnswerIndex in 0 until options.size) { "Correct answer index $correctAnswerIndex is out of bounds for options list of size ${options.size}" }
    }
}