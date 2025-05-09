package com.example.personaltutorapp.model

import com.example.personaltutorapp.data.dao.LessonPageDao

suspend fun LessonEntity.toLessonWithPages(pageDao: LessonPageDao): Lesson {
    val pagesFromDb = pageDao.getPagesForLesson(id)
        .map { it.toLessonPage() }

    return Lesson(
        id = this.id,
        title = this.title,
        pages = pagesFromDb.toMutableList(),
        completedByUserIds = this.completedByUserIds.toMutableList()
    )
}

fun LessonEntity.toLesson(): Lesson {
    return Lesson(
        id = id,
        title = title,
        pages = pages.toMutableList(),
        completedByUserIds = completedByUserIds.toMutableList()
    )
}

fun Lesson.toEntity(courseId: String): LessonEntity {
    return LessonEntity(
        id = id,
        courseId = courseId,
        title = title,
        pages = pages,
        completedByUserIds = completedByUserIds
    )
}


