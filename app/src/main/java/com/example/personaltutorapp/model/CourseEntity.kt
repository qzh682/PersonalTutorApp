package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses") // ✅ 确保使用正确的表名
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val tutorId: String,
    val enrolledUserIds: List<String> = emptyList(),
    val pendingUserIds: List<String> = emptyList()
)
