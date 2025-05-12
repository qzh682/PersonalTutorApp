package com.example.personaltutorapp.model

fun LessonPageEntity.toLessonPage(): LessonPage {
    println("Converting LessonPageEntity to LessonPage: id=$id")
    return LessonPage(
        id = id,
        type = type,
        content = content
    )
}

fun LessonPage.toEntity(lessonId: String): LessonPageEntity {
    println("Converting LessonPage to LessonPageEntity: id=$id, lessonId=$lessonId")
    return LessonPageEntity(
        id = id,
        lessonId = lessonId,
        type = type,
        content = content
    )
}