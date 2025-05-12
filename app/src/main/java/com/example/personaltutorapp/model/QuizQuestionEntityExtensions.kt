package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.AppTypeConverters

fun QuizQuestionEntity.toModel(): QuizQuestion {
    println("Converting QuizQuestionEntity to QuizQuestion: id=$id")
    val converters = AppTypeConverters()
    return QuizQuestion(
        id = id,
        quizId = quizId,
        question = question,
        options = converters.toStringList(options), // Use AppTypeConverters instance
        correctAnswerIndex = correctAnswerIndex
    )
}

fun QuizQuestion.toEntity(quizId: String): QuizQuestionEntity {
    println("Converting QuizQuestion to QuizQuestionEntity: id=$id, quizId=$quizId")
    val converters = AppTypeConverters()
    return QuizQuestionEntity(
        id = id,
        quizId = quizId,
        question = question,
        options = converters.fromStringList(options), // Use AppTypeConverters instance
        correctAnswerIndex = correctAnswerIndex
    )
}