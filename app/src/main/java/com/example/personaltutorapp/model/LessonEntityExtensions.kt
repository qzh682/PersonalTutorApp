package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.dao.LessonPageDao

suspend fun LessonEntity.toLesson(pageDao: LessonPageDao): Lesson {
    println("Converting LessonEntity to Lesson: id=$id")
    val pagesFromDb = pageDao.getPagesForLesson(id)
        .map { it.toLessonPage() }
    return Lesson(
        id = id,
        title = title,
        completedByUserIds = completedByUserIds.toMutableList(),
        pages = pagesFromDb.toMutableList()
    )
}

fun Lesson.toEntity(courseId: String): LessonEntity {
    println("Converting Lesson to LessonEntity: id=$id")
    return LessonEntity(
        id = id,
        courseId = courseId,
        title = title,
        completedByUserIds = completedByUserIds
    )
}