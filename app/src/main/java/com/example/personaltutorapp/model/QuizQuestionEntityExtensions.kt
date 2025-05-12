package com.example.personaltutorapp.model

fun QuizQuestionEntity.toModel(): QuizQuestion {
    println("Converting QuizQuestionEntity to QuizQuestion: id=$id")
    return QuizQuestion(
        id = id,
        question = question,
        options = options,
        correctAnswerIndex = correctAnswerIndex
    )
}

fun QuizQuestion.toEntity(quizId: String): QuizQuestionEntity {
    println("Converting QuizQuestion to QuizQuestionEntity: id=$id, quizId=$quizId")
    return QuizQuestionEntity(
        id = id,
        question = question,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        quizId = quizId
    )
}