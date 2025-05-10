package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.dao.*
import com.example.personaltutorapp.model.toModel

suspend fun CourseEntity.toCourseWithLessons(
    userDao: UserDao,
    lessonDao: LessonDao,
    lessonPageDao: LessonPageDao,
    quizDao: QuizDao,
    quizQuestionDao: QuizQuestionDao,
    quizSubmissionDao: QuizSubmissionDao
): Course {
    val tutor = userDao.getUserById(tutorId)?.toUser()
        ?: throw IllegalStateException("Tutor not found for ID $tutorId")

    val lessonEntities = lessonDao.getLessonsForCourse(id)
    val lessons = lessonEntities.map { it.toLessonWithPages(lessonPageDao) }

    val quizEntity = quizDao.getQuizForCourse(id)
    val quizQuestions = quizEntity?.let { quizQuestionDao.getQuestionsForQuiz(it.id) } ?: emptyList()
    val quizSubmissions = quizSubmissionDao.getSubmissionsForCourse(id)

    val quiz = quizEntity?.let {
        Quiz(
            id = it.id,
            courseId = id,
            questions = quizQuestions.map { q -> q.toModel() },
            submissions = quizSubmissions.map { s -> s.toModel() }
        )
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
