package com.example.personaltutorapp.model

data class Course(
    val id: String,
    val title: String,
    val description: String,
    val subject: String,
    val tutor: User,
    val lessons: MutableList<Lesson> = mutableListOf(),
    val enrolledUserIds: MutableList<String> = mutableListOf(),
    val pendingUserIds: MutableList<String> = mutableListOf()
)
