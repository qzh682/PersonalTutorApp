package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.dao.QuizQuestionDao
import com.example.personaltutorapp.data.dao.QuizSubmissionDao

suspend fun QuizEntity.toQuiz(
    questionDao: QuizQuestionDao,
    submissionDao: QuizSubmissionDao
): Quiz {
    println("Converting QuizEntity to Quiz: id=$id, isPublished=$isPublished")
    val questions = questionDao.getQuestionsForQuiz(id).map { it.toModel() }
    println("Fetched questions for quiz $id: ${questions.map { it.question }}")
    val submissions = submissionDao.getSubmissionsForCourse(courseId).map { it.toModel() }
    return Quiz(
        id = id,
        courseId = courseId,
        questions = questions,
        submissions = submissions,
        isPublished = isPublished
    )
}

fun Quiz.toEntity(): QuizEntity {
    println("Converting Quiz to QuizEntity: id=$id, isPublished=$isPublished")
    return QuizEntity(
        id = id,
        courseId = courseId,
        isPublished = isPublished
    )
}