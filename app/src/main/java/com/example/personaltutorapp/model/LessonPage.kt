package com.example.personaltutorapp.model

data class LessonPage(
    val id: String,
    val type: PageType, // TEXT or IMAGE
    val content: String
)

enum class PageType {
    TEXT,
    IMAGE
}
