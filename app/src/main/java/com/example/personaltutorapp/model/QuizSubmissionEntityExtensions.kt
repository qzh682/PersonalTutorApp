package com.example.personaltutorapp.model

fun QuizSubmission.toEntity(courseId: String): QuizSubmissionEntity {
    return QuizSubmissionEntity(
        id = "$courseId-$userId",
        courseId = courseId,
        userId = userId,
        score = score
    )
}

fun QuizSubmissionEntity.toModel(): QuizSubmission {
    return QuizSubmission(
        userId = userId,
        score = score
    )
}


