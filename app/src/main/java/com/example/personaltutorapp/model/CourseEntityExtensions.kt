// 文件路径建议：com/example/personaltutorapp/model/CourseExtensions.kt

package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.dao.LessonDao
import com.example.personaltutorapp.data.dao.UserDao
import com.example.personaltutorapp.data.dao.LessonPageDao

suspend fun CourseEntity.toCourseWithLessons(
    userDao: UserDao,
    lessonDao: LessonDao
): Course {
    val tutor = userDao.getUserById(tutorId)?.toUser()
        ?: throw IllegalStateException("Tutor not found for ID $tutorId")

    val lessonEntities = lessonDao.getLessonsForCourse(id)
    val lessons = lessonEntities.map { it.toLesson() }

    return Course(
        id = this.id,
        title = this.title,
        description = this.description,
        subject = this.subject,
        tutor = tutor,
        lessons = lessons.toMutableList(),
        enrolledUserIds = this.enrolledUserIds.toMutableList(),
        pendingUserIds = this.pendingUserIds.toMutableList()
    )
}

suspend fun CourseEntity.toCourseWithLessons(
    userDao: UserDao,
    lessonDao: LessonDao,
    lessonPageDao: LessonPageDao
): Course {
    val tutor = userDao.getUserById(tutorId)?.toUser()
        ?: throw IllegalStateException("Tutor not found for ID $tutorId")

    val lessonEntities = lessonDao.getLessonsForCourse(id)
    val lessons = lessonEntities.map { it.toLessonWithPages(lessonPageDao) }

    return Course(
        id = this.id,
        title = this.title,
        description = this.description,
        subject = this.subject,
        tutor = tutor,
        lessons = lessons.toMutableList(),
        enrolledUserIds = this.enrolledUserIds.toMutableList(),
        pendingUserIds = this.pendingUserIds.toMutableList()
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

