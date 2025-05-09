package com.example.personaltutorapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val password: String,
    val displayName: String,
    val role: String, // "Student" or "Tutor"
    val bio: String,
    val profileImageUrl: String
)
