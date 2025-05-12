package com.example.personaltutorapp.model

fun QuizSubmission.toEntity(id: String, courseId: String): QuizSubmissionEntity {
    println("Converting QuizSubmission to QuizSubmissionEntity: id=$id, userId=$userId")
    return QuizSubmissionEntity(
        id = id,
        courseId = courseId,
        userId = userId,
        score = score
    )
}

fun QuizSubmissionEntity.toModel(): QuizSubmission {
    println("Converting QuizSubmissionEntity to QuizSubmission: id=$id")
    return QuizSubmission(
        userId = userId,
        score = score
    )
}