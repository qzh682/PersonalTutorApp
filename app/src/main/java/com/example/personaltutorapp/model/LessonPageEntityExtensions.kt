package com.example.personaltutorapp.model

fun LessonPageEntity.toLessonPage(): LessonPage {
    return LessonPage(
        id = id,
        type = type,
        content = content
    )
}

fun LessonPage.toEntity(lessonId: String): LessonPageEntity {
    return LessonPageEntity(
        id = id,
        lessonId = lessonId,
        type = type,
        content = content
    )
}

