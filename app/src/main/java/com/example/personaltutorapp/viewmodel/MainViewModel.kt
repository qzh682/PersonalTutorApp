package com.example.personaltutorapp.viewmodel

import androidx.lifecycle.ViewModel
import com.example.personaltutorapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

class MainViewModel : ViewModel() {

    private val _users = mutableListOf<User>()
    private val _courses = mutableListOf<Course>()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    val allCourses: List<Course>
        get() = _courses

    fun register(
        email: String,
        password: String,
        displayName: String,
        role: String,
        bio: String,
        profileImageUrl: String
    ): Boolean {
        if (_users.any { it.email == email }) return false
        val newUser = User(
            id = UUID.randomUUID().toString(),
            email = email,
            password = password,
            displayName = displayName,
            role = role,
            bio = bio,
            profileImageUrl = profileImageUrl
        )
        _users.add(newUser)
        _currentUser.value = newUser
        return true
    }

    fun login(email: String, password: String): Boolean {
        val user = _users.find { it.email == email && it.password == password }
        return if (user != null) {
            _currentUser.value = user
            true
        } else {
            false
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun createCourse(title: String, description: String, subject: String) {
        val course = Course(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            subject = subject,
            tutor = _currentUser.value!!,
            lessons = mutableListOf()
        )
        _courses.add(course)
    }

    fun getCoursesForCurrentUser(): List<Course> {
        return _courses.filter { it.tutor == _currentUser.value }
    }

    fun getCourseById(courseId: String): Course? {
        return _courses.find { it.id == courseId }
    }

    fun addLessonToCourse(courseId: String, title: String, pages: List<LessonPage>) {
        val course = getCourseById(courseId)
        course?.lessons?.add(
            Lesson(
                id = UUID.randomUUID().toString(),
                title = title,
                pages = pages.toMutableList(),
                completedByUserIds = mutableListOf()
            )
        )
    }

    fun markLessonCompleted(courseId: String, lessonId: String) {
        val userId = _currentUser.value?.id ?: return
        val course = getCourseById(courseId)
        val lesson = course?.lessons?.find { it.id == lessonId }
        lesson?.completedByUserIds?.add(userId)
    }
}
