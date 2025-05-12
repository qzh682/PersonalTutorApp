package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.dao.*

suspend fun CourseEntity.toCourseWithLessons(
    userDao: UserDao,
    lessonDao: LessonDao,
    lessonPageDao: LessonPageDao,
    quizDao: QuizDao,
    quizQuestionDao: QuizQuestionDao,
    quizSubmissionDao: QuizSubmissionDao
): Course {
    println("Converting CourseEntity to Course: id=$id")
    val tutor = try {
        userDao.getUserById(tutorId)?.toUser()
            ?: throw IllegalStateException("Tutor not found for ID $tutorId")
    } catch (e: Exception) {
        println("Failed to load tutor for course $id: ${e.message}")
        throw e
    }

    val lessonEntities = lessonDao.getLessonsForCourse(id)
    val lessons = lessonEntities.map { lessonEntity ->
        try {
            lessonEntity.toLesson(lessonPageDao)
        } catch (e: Exception) {
            println("Failed to convert lesson ${lessonEntity.id} for course $id: ${e.message}")
            throw e
        }
    }

    val quizEntity = quizDao.getQuizForCourse(id)
    val quiz = quizEntity?.let {
        try {
            val quizQuestions = quizQuestionDao.getQuestionsForQuiz(it.id)
            val quizSubmissions = quizSubmissionDao.getSubmissionsForCourse(id)
            Quiz(
                id = it.id,
                courseId = id,
                questions = quizQuestions.map { q -> q.toModel() },
                submissions = quizSubmissions.map { s -> s.toModel() }
            )
        } catch (e: Exception) {
            println("Failed to load quiz for course $id: ${e.message}")
            null
        }
    }

    return Course(
        id = this.id,
        title = this.title,
        description = this.description,
        subject = this.subject,
        tutor = tutor,
        lessons = lessons.toMutableList(),
        enrolledUserIds = this.enrolledUserIds.toMutableList(),
        pendingUserIds = this.pendingUserIds.toMutableList(),
        quiz = quiz
    )
}

fun Course.toEntity(): CourseEntity {
    println("Converting Course to CourseEntity: id=$id")
    return CourseEntity(
        id = this.id,
        title = this.title,
        description = this.description,
        subject = this.subject,
        tutorId = this.tutor.id,
        enrolledUserIds = this.enrolledUserIds,
        pendingUserIds = this.pendingUserIds
    )
}