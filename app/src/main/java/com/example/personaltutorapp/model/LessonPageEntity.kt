package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.regex.Pattern

@Entity(tableName = "lesson_pages")
data class LessonPageEntity(
    @PrimaryKey val id: String,
    val lessonId: String, // 外键，指向 LessonEntity.id
    val type: PageType,
    val content: String
) {
    init {
        require(id.isNotBlank()) { "Lesson page ID cannot be blank" }
        require(lessonId.isNotBlank()) { "Lesson ID cannot be blank" }
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