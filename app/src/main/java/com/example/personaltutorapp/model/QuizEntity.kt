package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.personaltutorapp.db.Converters

@Entity(tableName = "quizzes")
@TypeConverters(Converters::class)
data class QuizEntity(
    @PrimaryKey val id: String,
    val courseId: String,
    val questions: List<QuizQuestion>,
    val submissions: List<QuizSubmission> = emptyList() // ✅ 添加这一行
)
