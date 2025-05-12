package com.example.personaltutorapp.model

import java.util.regex.Pattern

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
) {
    init {
        require(id.isNotBlank()) { "Lesson page ID cannot be blank" }
        require(content.isNotBlank()) { "Content cannot be blank" }
        when (type) {
            PageType.IMAGE -> validateImageUrl(content)
            PageType.PDF -> validateFileUrl(content, "pdf")
            PageType.AUDIO -> validateFileUrl(content, "mp3")
            PageType.VIDEO -> validateFileUrl(content, "mp4")
            else -> Unit // TEXT 类型无需额外验证
        }
    }

    private fun validateImageUrl(url: String) {
        val pattern = Pattern.compile(
            "^(https?://.*\\.(?:jpg|jpeg|png|gif))$",
            Pattern.CASE_INSENSITIVE
        )
        require(pattern.matcher(url).matches()) { "Invalid image URL: Must end with jpg, jpeg, png, or gif" }
    }

    private fun validateFileUrl(url: String, extension: String) {
        val pattern = Pattern.compile(
            "^(https?://.*\\.$extension)$",
            Pattern.CASE_INSENSITIVE
        )
        require(pattern.matcher(url).matches()) { "Invalid $extension URL: Must end with $extension" }
    }
}