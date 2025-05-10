package com.example.personaltutorapp.model

fun QuizQuestionEntity.toQuizQuestion(): QuizQuestion {
    return QuizQuestion(
        id = id,
        question = question,
        options = options,
        correctAnswerIndex = correctAnswerIndex
    )
}

fun QuizQuestion.toEntity(quizId: String): QuizQuestionEntity {
    return QuizQuestionEntity(
        id = id,
        question = question,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        quizId = quizId
    )
}


fun QuizQuestionEntity.toModel(): QuizQuestion {
    return QuizQuestion(
        id = id,
        question = question,
        options = options,
        correctAnswerIndex = correctAnswerIndex
    )
}
