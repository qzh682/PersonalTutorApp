package com.example.personaltutorapp.model

data class User(
    val id: String,
    val email: String,
    val password: String,
    val displayName: String,
    val role: String, // "Tutor" or "Student"
    val bio: String,
    val profileImageUrl: String
)
