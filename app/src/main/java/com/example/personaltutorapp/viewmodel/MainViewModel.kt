package com.example.personaltutorapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltutorapp.data.AppDatabase
import com.example.personaltutorapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val courseDao = db.courseDao()
    private val lessonPageDao = db.lessonPageDao()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses

    init {
        viewModelScope.launch {
            refreshCourses()
        }
    }

    private suspend fun refreshCourses() {
        val entities = courseDao.getAllCourses()
        _allCourses.value = entities.map {
            it.toCourseWithLessons(userDao, db.lessonDao(), db.lessonPageDao())
        }
    }



    fun register(
        email: String,
        password: String,
        displayName: String,
        role: String,
        bio: String,
        profileImageUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                onResult(false)
                return@launch
            }

            val newUser = User(
                id = UUID.randomUUID().toString(),
                email = email,
                password = password,
                displayName = displayName,
                role = role,
                bio = bio,
                profileImageUrl = profileImageUrl
            )
            userDao.insertUser(newUser.toEntity())
            _currentUser.value = newUser
            onResult(true)
        }
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userEntity = userDao.getUserByEmail(email)
            if (userEntity != null && userEntity.password == password) {
                _currentUser.value = userEntity.toUser()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun createCourse(title: String, description: String, subject: String) = viewModelScope.launch {
        val course = Course(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            subject = subject,
            tutor = currentUser.value!!,
            lessons = mutableListOf(),
            enrolledUserIds = mutableListOf(),
            pendingUserIds = mutableListOf()
        )
        courseDao.insertCourse(course.toEntity())
        refreshCourses()
    }

    fun getCoursesForCurrentUser(): List<Course> {
        val tutorId = _currentUser.value?.id ?: return emptyList()
        return _allCourses.value.filter { it.tutor.id == tutorId }
    }

    fun getCourseById(courseId: String): Course? {
        return _allCourses.value.find { it.id == courseId }
    }

    fun addLessonToCourse(courseId: String, title: String, pages: List<LessonPage>) = viewModelScope.launch {
        val course = getCourseById(courseId) ?: return@launch

        // 1. 创建新 LessonEntity
        val lessonId = UUID.randomUUID().toString()
        val newLesson = LessonEntity(
            id = lessonId,
            courseId = courseId,
            title = title,
            pages = emptyList(), // 页面单独存储
            completedByUserIds = emptyList()
        )
        db.lessonDao().insertLesson(newLesson)

        // 2. 批量插入 LessonPageEntity
        pages.forEach { page ->
            val pageEntity = LessonPageEntity(
                id = page.id,
                lessonId = lessonId,
                type = page.type,
                content = page.content
            )
            lessonPageDao.insertPage(pageEntity)
        }

        // 3. 刷新数据
        refreshCourses()
    }


    fun markLessonCompleted(courseId: String, lessonId: String) = viewModelScope.launch {
        val userId = _currentUser.value?.id ?: return@launch
        val course = getCourseById(courseId) ?: return@launch
        val updatedLessons = course.lessons.map {
            if (it.id == lessonId && !it.completedByUserIds.contains(userId)) {
                it.copy(completedByUserIds = (it.completedByUserIds + userId).toMutableList())
            } else it
        }
        val updatedCourse = course.copy(lessons = updatedLessons.toMutableList())
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshCourses()
    }

    fun enrollInCourse(courseId: String) = viewModelScope.launch {
        val userId = _currentUser.value?.id ?: return@launch
        val course = getCourseById(courseId) ?: return@launch
        if (!course.pendingUserIds.contains(userId) && !course.enrolledUserIds.contains(userId)) {
            val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds + userId).toMutableList())
            courseDao.updateCourse(updatedCourse.toEntity())
            refreshCourses()
        }
    }

    fun unenrollFromCourse(courseId: String) = viewModelScope.launch {
        val userId = _currentUser.value?.id ?: return@launch
        val course = getCourseById(courseId) ?: return@launch
        val updatedCourse = course.copy(enrolledUserIds = (course.enrolledUserIds - userId).toMutableList())
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshCourses()
    }

    fun isEnrolled(courseId: String): Boolean {
        val userId = _currentUser.value?.id ?: return false
        return getCourseById(courseId)?.enrolledUserIds?.contains(userId) == true
    }

    suspend fun getPendingRequests(courseId: String): List<User> {
        val course = getCourseById(courseId)
        return course?.pendingUserIds
            ?.mapNotNull { userDao.getUserById(it)?.toUser() }
            ?: emptyList()
    }

    fun acceptEnrollment(courseId: String, userId: String) = viewModelScope.launch {
        val course = getCourseById(courseId) ?: return@launch
        val updatedCourse = course.copy(
            pendingUserIds = (course.pendingUserIds - userId).toMutableList(),
            enrolledUserIds = (course.enrolledUserIds + userId).toMutableList()
        )
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshCourses()
    }

    fun rejectEnrollment(courseId: String, userId: String) = viewModelScope.launch {
        val course = getCourseById(courseId) ?: return@launch
        val updatedCourse = course.copy(
            pendingUserIds = (course.pendingUserIds - userId).toMutableList()
        )
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshCourses()
    }


    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    suspend fun getUserByIdSuspend(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    fun canAccessLesson(lessons: List<Lesson>, index: Int, userId: String?): Boolean {
        if (userId == null) return false
        return index == 0 || lessons[index - 1].completedByUserIds.contains(userId)
    }

    // 🔽 LessonPage 数据持久化操作

    fun addLessonPage(lessonId: String, type: PageType, content: String) = viewModelScope.launch {
        val page = LessonPageEntity(
            id = UUID.randomUUID().toString(),
            lessonId = lessonId,
            type = type,
            content = content
        )
        lessonPageDao.insertPage(page)
    }

    fun updateLessonPage(page: LessonPageEntity) = viewModelScope.launch {
        lessonPageDao.updatePage(page)
    }

    fun deleteLessonPage(page: LessonPageEntity) = viewModelScope.launch {
        lessonPageDao.deletePage(page)
    }

    suspend fun getLessonPages(lessonId: String): List<LessonPageEntity> {
        return lessonPageDao.getPagesForLesson(lessonId)
    }
}
