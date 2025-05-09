package com.example.personaltutorapp.model

enum class PageType {
    TEXT,
    IMAGE,
    PDF,
    AUDIO,
    VIDEO
}

data class LessonPage(
    val id: String,
    val type: PageType,
    val content: String // 可为纯文本、图片链接、多媒体文件 URL 等
)
