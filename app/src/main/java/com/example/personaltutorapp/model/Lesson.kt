package com.example.personaltutorapp.model

data class Lesson(
    val id: String,
    val title: String,
    val completedByUserIds: MutableList<String>,
    val pages: MutableList<LessonPage> = mutableListOf() // 页面从数据库加载
) {
    init {
        require(id.isNotBlank()) { "Lesson ID cannot be blank" }
        require(title.isNotBlank()) { "Lesson title cannot be blank" }
    }
}