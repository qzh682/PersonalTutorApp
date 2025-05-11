package com.example.personaltutorapp.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.personaltutorapp.data.AppDatabase
import com.example.personaltutorapp.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import com.example.personaltutorapp.model.toEntity

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val courseDao = db.courseDao()
    private val lessonPageDao = db.lessonPageDao()
    private val quizSubmissionDao = db.quizSubmissionDao()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses

    private val appContext = application.applicationContext

    init {
        viewModelScope.launch {
            insertTestUsers()
            refreshAllCourses()
        }
    }

    suspend fun refreshAllCourses() {
        try {
            val entities = courseDao.getAllCourses()
            val courses = entities.map {
                it.toCourseWithLessons(userDao, db.lessonDao(), db.lessonPageDao(), db.quizDao(), db.quizQuestionDao(), db.quizSubmissionDao())
            }
            _allCourses.value = courses
            println("All courses refreshed successfully: ${courses.map { "${it.id} (Lessons: ${it.lessons.size})" }}")
        } catch (e: Exception) {
            println("Failed to refresh all courses: ${e.message}")
        }
    }

    fun insertTestUsers() = viewModelScope.launch {
        val testUsers = listOf(
            User(
                id = UUID.randomUUID().toString(),
                email = "test1@example.com",
                password = "test123",
                displayName = "Test Student 1",
                role = "Student",
                bio = "",
                profileImageUrl = ""
            ),
            User(
                id = UUID.randomUUID().toString(),
                email = "test2@temp-mail.org",
                password = "test123",
                displayName = "Test Student 2",
                role = "Student",
                bio = "",
                profileImageUrl = ""
            )
        )
        testUsers.forEach { user ->
            userDao.insertUser(user.toEntity())
            println("Inserted test user: ${user.email}")
        }
    }

    fun enrollTestUser(courseId: String, email: String) = viewModelScope.launch {
        val user = userDao.getUserByEmail(email)?.toUser() ?: run {
            println("User $email not found")
            return@launch
        }
        val course = getCourseById(courseId) ?: run {
            println("Course $courseId not found")
            return@launch
        }
        if (!course.pendingUserIds.contains(user.id) && !course.enrolledUserIds.contains(user.id)) {
            val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds + user.id).toMutableList())
            courseDao.updateCourse(updatedCourse.toEntity())
            refreshAllCourses()
            acceptEnrollment(courseId, user.id)
            println("Enrolled test user ${user.email} to course $courseId")
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
            if (!isValidEmail(email)) {
                println("Invalid email format: $email")
                onResult(false)
                return@launch
            }
            val existing = userDao.getUserByEmail(email)
            if (existing != null) {
                println("Email $email already exists")
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
            println("Registered user: ${newUser.email}")
            onResult(true)
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(email: String, password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val userEntity = userDao.getUserByEmail(email)
            if (userEntity != null && userEntity.password == password) {
                _currentUser.value = userEntity.toUser()
                println("Logged in user: $email")
                onResult(true)
            } else {
                println("Login failed for $email")
                onResult(false)
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        println("Logged out")
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
        refreshAllCourses()
        println("Created course: ${course.title}")
    }

    fun getCoursesForCurrentUser(): List<Course> {
        val tutorId = _currentUser.value?.id ?: return emptyList()
        return _allCourses.value.filter { it.tutor.id == tutorId }
    }

    fun getCourseById(courseId: String): Course? {
        return _allCourses.value.find { it.id == courseId }
    }

    fun addLessonToCourse(courseId: String, title: String, pages: List<LessonPage>) = viewModelScope.launch {
        val course = getCourseById(courseId) ?: run {
            println("Course $courseId not found")
            return@launch
        }
        println("Adding lesson to course: ${course.title}, enrolledUserIds: ${course.enrolledUserIds}")

        try {
            val lessonId = UUID.randomUUID().toString()
            val newLesson = LessonEntity(
                id = lessonId,
                courseId = courseId,
                title = title,
                pages = emptyList(),
                completedByUserIds = emptyList()
            )
            db.lessonDao().insertLesson(newLesson)
            println("Inserted lesson: $title (ID: $lessonId)")

            pages.forEachIndexed { index, page ->
                val pageEntity = LessonPageEntity(
                    id = page.id,
                    lessonId = lessonId,
                    type = page.type,
                    content = page.content
                )
                lessonPageDao.insertPage(pageEntity)
                println("Inserted page ${index + 1}/${pages.size}: Type=${page.type}, Content=${page.content}")
            }

            refreshAllCourses()
            println("Lesson $title added to course ${course.title}")

            if (course.enrolledUserIds.isEmpty()) {
                println("No enrolled users for course ${course.title}")
            } else {
                course.enrolledUserIds.forEach { userId ->
                    val user = userDao.getUserById(userId)?.toUser()
                    println("Checking user for ID $userId: $user")
                    user?.let {
                        println("Sending email to ${it.email} for new lesson: $title")
                        sendEmail(it.email, "New Lesson: $title", "A new lesson has been added to your enrolled course: ${course.title}.")
                    } ?: println("User $userId not found")
                }
            }
        } catch (e: Exception) {
            println("Failed to add lesson to course $courseId: ${e.message}")
        }
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            val chooser = Intent.createChooser(intent, "Send Email")
            if (intent.resolveActivity(appContext.packageManager) != null) {
                appContext.startActivity(chooser)
                println("✅ Starting email client for $to with chooser")
            } else {
                println("❌ No email client found for ACTION_SEND")
            }
        } catch (e: Exception) {
            println("❌ Failed to start email client for $to: ${e.message}")
        }
    }

    suspend fun markLessonCompleted(courseId: String, lessonId: String) {
        val userId = _currentUser.value?.id ?: return
        val lessonEntity = db.lessonDao().getLessonById(lessonId) ?: return

        println("Before update: Lesson $lessonId, completedByUserIds: ${lessonEntity.completedByUserIds}")
        if (!lessonEntity.completedByUserIds.contains(userId)) {
            val updatedCompletedByUserIds = lessonEntity.completedByUserIds.toMutableList().apply {
                add(userId)
            }
            val updatedLesson = lessonEntity.copy(completedByUserIds = updatedCompletedByUserIds)
            db.lessonDao().updateLesson(updatedLesson)
            val updated = db.lessonDao().getLessonById(lessonId)
            println("After update: Lesson $lessonId, completedByUserIds: ${updated?.completedByUserIds}")
            refreshAllCourses()
        }
    }

    fun enrollInCourse(courseId: String) = viewModelScope.launch {
        val userId = _currentUser.value?.id ?: return@launch
        val course = getCourseById(courseId) ?: return@launch
        if (!course.pendingUserIds.contains(userId) && !course.enrolledUserIds.contains(userId)) {
            val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds + userId).toMutableList())
            courseDao.updateCourse(updatedCourse.toEntity())
            refreshAllCourses()
        }
    }

    fun unenrollFromCourse(courseId: String) = viewModelScope.launch {
        val userId = _currentUser.value?.id ?: return@launch
        val course = getCourseById(courseId) ?: return@launch
        val updatedCourse = course.copy(enrolledUserIds = (course.enrolledUserIds - userId).toMutableList())
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshAllCourses()
    }

    fun isEnrolled(courseId: String): Boolean {
        val userId = _currentUser.value?.id ?: return false
        return getCourseById(courseId)?.enrolledUserIds?.contains(userId) == true
    }

    suspend fun getPendingRequests(courseId: String): List<User> {
        val course = getCourseById(courseId)
        return course?.pendingUserIds?.mapNotNull { userDao.getUserById(it)?.toUser() } ?: emptyList()
    }

    fun acceptEnrollment(courseId: String, userId: String) = viewModelScope.launch {
        println("Accepting enrollment for course $courseId, user $userId")
        val course = getCourseById(courseId) ?: run {
            println("Course $courseId not found")
            return@launch
        }
        val updatedCourse = course.copy(
            pendingUserIds = (course.pendingUserIds - userId).toMutableList(),
            enrolledUserIds = (course.enrolledUserIds + userId).toMutableList()
        )
        println("Updating course: pendingUserIds=${updatedCourse.pendingUserIds}, enrolledUserIds=${updatedCourse.enrolledUserIds}")
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshAllCourses()
        println("Course $courseId updated and refreshed")
    }

    fun rejectEnrollment(courseId: String, userId: String) = viewModelScope.launch {
        println("Rejecting enrollment for course $courseId, user $userId")
        val course = getCourseById(courseId) ?: run {
            println("Course $courseId not found")
            return@launch
        }
        val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds - userId).toMutableList())
        println("Updating course: pendingUserIds=${updatedCourse.pendingUserIds}")
        courseDao.updateCourse(updatedCourse.toEntity())
        refreshAllCourses()
        println("Course $courseId updated and refreshed")
    }

    suspend fun getUserById(userId: String): User? {
        return userDao.getUserById(userId)?.toUser()
    }

    fun canAccessLesson(lessons: List<Lesson>, index: Int, userId: String?): Boolean {
        if (userId == null) return false
        return index == 0 || lessons[index - 1].completedByUserIds.contains(userId)
    }

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

    data class QuizResult(val studentName: String, val score: Int, val total: Int)

    suspend fun getQuizResults(courseId: String): List<QuizResult> {
        val course = getCourseById(courseId) ?: return emptyList()
        val quiz = course.quiz ?: return emptyList()
        val total = quiz.questions.size

        return quiz.submissions.mapNotNull { submission ->
            val user = userDao.getUserById(submission.userId)?.toUser()
            user?.let {
                QuizResult(
                    studentName = it.displayName,
                    score = submission.score,
                    total = total
                )
            }
        }
    }

    fun submitQuizResult(courseId: String, userId: String, score: Int) = viewModelScope.launch {
        val quiz = db.quizDao().getQuizForCourse(courseId)
        val existing = quizSubmissionDao.getSubmission(userId, courseId)
        if (existing != null) {
            quizSubmissionDao.deleteSubmission(existing)
        }
        val submission = QuizSubmissionEntity(
            id = UUID.randomUUID().toString(),
            courseId = courseId,
            userId = userId,
            score = score
        )
        quizSubmissionDao.insertSubmission(submission)
        refreshAllCourses()
    }

    fun addQuizToCourse(courseId: String, questions: List<QuizQuestion>) = viewModelScope.launch {
        val course = getCourseById(courseId) ?: return@launch

        val quizId = UUID.randomUUID().toString()

        val quizEntity = QuizEntity(
            id = quizId,
            courseId = courseId,
            questions = emptyList(),
            submissions = emptyList()
        )
        db.quizDao().insertQuiz(quizEntity)

        val questionEntities = questions.map { it.toEntity(quizId) }
        db.quizQuestionDao().insertAll(questionEntities)

        refreshAllCourses()
    }

    suspend fun debugCourse(courseId: String) {
        val course = db.courseDao().getCourseById(courseId)
        println("Course: $course")
    }

    suspend fun debugUser(userId: String) {
        val user = db.userDao().getUserById(userId)
        println("User: $user")
    }

    suspend fun deleteTestUser(email: String) {
        val user = db.userDao().getUserByEmail(email)
        user?.let { userDao.deleteUser(it) }
        println("Deleted test user: $email")
    }
}