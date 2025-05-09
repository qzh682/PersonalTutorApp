package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val tutorId: String, // 外键指向 UserEntity.id
    val enrolledUserIds: List<String>, // 存为 JSON 或支持的格式
    val pendingUserIds: List<String>   // 存为 JSON 或支持的格式
)
