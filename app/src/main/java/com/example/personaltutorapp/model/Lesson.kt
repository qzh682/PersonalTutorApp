package com.example.personaltutorapp.model

data class Lesson(
    val id: String,
    val title: String,
    val pages: MutableList<LessonPage>,
    val completedByUserIds: MutableList<String>
)
