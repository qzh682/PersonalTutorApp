package com.example.personaltutorapp.model

fun QuizEntity.toQuiz(): Quiz {
    return Quiz(
        id = this.id,
        courseId = this.courseId,
        questions = this.questions
    )
}

fun Quiz.toEntity(): QuizEntity {
    return QuizEntity(
        id = this.id,
        courseId = this.courseId,
        questions = this.questions
    )
}
