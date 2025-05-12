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
        require(options.size == 4) { "Options list must contain exactly 4 items" }
        require(options.all { it.isNotBlank() }) { "Options cannot be blank" }
        require(options.distinct().size == options.size) { "Options must be unique" }
        require(correctAnswerIndex in 0 until options.size) { "Correct answer index $correctAnswerIndex is out of bounds for options list of size ${options.size}" }
    }
}