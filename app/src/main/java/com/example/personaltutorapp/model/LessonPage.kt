// file: com/example/personaltutorapp/model/LessonPage.kt
package com.example.personaltutorapp.model

enum class PageType {
    TEXT,
    IMAGE
}

data class LessonPage(
    val id: String,
    val type: PageType,
    val content: String
)
