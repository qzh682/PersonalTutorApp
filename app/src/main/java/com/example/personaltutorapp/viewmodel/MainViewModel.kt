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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val courseDao = db.courseDao()
    private val lessonDao = db.lessonDao()
    private val lessonPageDao = db.lessonPageDao()
    private val quizDao = db.quizDao()
    private val quizQuestionDao = db.quizQuestionDao()
    private val quizSubmissionDao = db.quizSubmissionDao()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses

    private val _userByEmail = MutableStateFlow<User?>(null)
    val userByEmail: StateFlow<User?> = _userByEmail.asStateFlow()

    private val appContext = application.applicationContext

    init {
        viewModelScope.launch {
            try {
                insertTestUsers()
                refreshAllCourses()
            } catch (e: Exception) {
                logError("Initialization failed: ${e.message}")
            }
        }
    }

    suspend fun refreshAllCourses(): Result<Unit> {
        return try {
            val entities = courseDao.getAllCourses()
            val courses = entities.map {
                it.toCourseWithLessons(userDao, lessonDao, lessonPageDao, quizDao, quizQuestionDao, quizSubmissionDao)
            }
            _allCourses.value = courses
            logInfo("All courses refreshed successfully: ${courses.map { "${it.id} (Lessons: ${it.lessons.size}, Quiz: ${it.quiz != null}, Published: ${it.quiz?.isPublished})" }}")
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Failed to refresh all courses: ${e.message}")
            _allCourses.value = emptyList()
            Result.failure(e)
        }
    }

    private fun insertTestUsers() = viewModelScope.launch {
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
            try {
                userDao.insertUser(user.toEntity())
                logInfo("Inserted test user: ${user.email}")
            } catch (e: Exception) {
                logError("Failed to insert test user ${user.email}: ${e.message}")
            }
        }
    }

    fun enrollTestUser(courseId: String, email: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            val user = userDao.getUserByEmail(email)?.toUser() ?: run {
                logError("User $email not found")
                onResult(Result.failure(Exception("User $email not found")))
                return@launch
            }
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            if (!course.pendingUserIds.contains(user.id) && !course.enrolledUserIds.contains(user.id)) {
                val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds + user.id).toMutableList())
                courseDao.updateCourse(updatedCourse.toEntity())
                refreshAllCourses()
                acceptEnrollment(courseId, user.id) { }
                logInfo("Enrolled test user ${user.email} to course $courseId")
                onResult(Result.success(Unit))
            } else {
                logInfo("User ${user.email} already enrolled or pending for course $courseId")
                onResult(Result.success(Unit))
            }
        } catch (e: Exception) {
            logError("Failed to enroll test user $email to course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun register(
        email: String,
        password: String,
        displayName: String,
        role: String,
        bio: String,
        profileImageUrl: String,
        onResult: (Result<User>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                validateRegistrationInput(email, password, displayName)?.let { error ->
                    logError(error)
                    onResult(Result.failure(Exception(error)))
                    return@launch
                }

                val existing = userDao.getUserByEmail(email)
                if (existing != null) {
                    logError("Email $email already exists")
                    onResult(Result.failure(Exception("Email already exists")))
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
                logInfo("Registered user: ${newUser.email}")
                onResult(Result.success(newUser))
            } catch (e: Exception) {
                logError("Failed to register user $email: ${e.message}")
                onResult(Result.failure(e))
            }
        }
    }

    private fun validateRegistrationInput(email: String, password: String, displayName: String): String? {
        if (!isValidEmail(email)) {
            return "Invalid email format: $email"
        }
        if (password.length < 6) {
            return "Password too short: must be at least 6 characters"
        }
        if (displayName.isBlank()) {
            return "Display name cannot be blank"
        }
        return null
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun login(email: String, password: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            try {
                val userEntity = userDao.getUserByEmail(email)
                if (userEntity != null && userEntity.password == password) {
                    val user = userEntity.toUser()
                    _currentUser.value = user
                    logInfo("Logged in user: $email")
                    onResult(Result.success(user))
                } else {
                    logError("Login failed for $email: Invalid email or password")
                    onResult(Result.failure(Exception("Invalid email or password")))
                }
            } catch (e: Exception) {
                logError("Failed to login user $email: ${e.message}")
                onResult(Result.failure(e))
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        logInfo("Logged out")
    }

    fun fetchUserByEmail(email: String) {
        viewModelScope.launch {
            try {
                _userByEmail.value = if (email.isBlank()) null else userDao.getUserByEmail(email)?.toUser()
            } catch (e: Exception) {
                logError("Failed to fetch user by email $email: ${e.message}")
                _userByEmail.value = null
            }
        }
    }

    fun createCourse(title: String, description: String, subject: String, onResult: (Result<Course>) -> Unit) = viewModelScope.launch {
        try {
            if (title.isBlank()) {
                logError("Course title cannot be blank")
                onResult(Result.failure(Exception("Course title cannot be blank")))
                return@launch
            }
            if (subject.isBlank()) {
                logError("Course subject cannot be blank")
                onResult(Result.failure(Exception("Course subject cannot be blank")))
                return@launch
            }
            val currentUser = _currentUser.value ?: run {
                logError("No current user to create course")
                onResult(Result.failure(Exception("No current user")))
                return@launch
            }
            val course = Course(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                subject = subject,
                tutor = currentUser,
                lessons = mutableListOf(),
                enrolledUserIds = mutableListOf(),
                pendingUserIds = mutableListOf()
            )
            courseDao.insertCourse(course.toEntity())
            refreshAllCourses()
            logInfo("Created course: ${course.title}")
            onResult(Result.success(course))
        } catch (e: Exception) {
            logError("Failed to create course $title: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun getCoursesForCurrentUser(): List<Course> {
        val tutorId = _currentUser.value?.id ?: return emptyList()
        return _allCourses.value.filter { it.tutor.id == tutorId }
    }

    fun getCourseById(courseId: String): Course? {
        return _allCourses.value.find { it.id == courseId }
    }

    fun addLessonToCourse(courseId: String, title: String, pages: List<LessonPage>, onResult: (Result<LessonEntity>) -> Unit) = viewModelScope.launch {
        try {
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            validateLessonInput(title, pages)?.let { error ->
                logError("Validation failed for lesson: $error")
                onResult(Result.failure(Exception(error)))
                return@launch
            }

            val lessonId = UUID.randomUUID().toString()
            val newLesson = LessonEntity(
                id = lessonId,
                courseId = courseId,
                title = title,
                completedByUserIds = emptyList()
            )
            insertLessonWithPages(newLesson, pages)
            logInfo("Lesson $title added to course ${course.title}")
            notifyEnrolledUsers(course, title)
            onResult(Result.success(newLesson))
        } catch (e: Exception) {
            logError("Failed to add lesson to course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    private fun validateLessonInput(title: String, pages: List<LessonPage>): String? {
        return when {
            title.isBlank() -> "Lesson title cannot be blank"
            pages.isEmpty() -> "Lesson must have at least one page"
            else -> null
        }
    }

    private suspend fun insertLessonWithPages(lesson: LessonEntity, pages: List<LessonPage>) {
        lessonDao.insertLesson(lesson)
        logInfo("Inserted lesson: ${lesson.title} (ID: ${lesson.id})")
        pages.forEachIndexed { index, page ->
            val pageEntity = LessonPageEntity(
                id = page.id,
                lessonId = lesson.id,
                type = page.type,
                content = page.content
            )
            lessonPageDao.insertPage(pageEntity)
            logInfo("Inserted page ${index + 1}/${pages.size}: Type=${page.type}, Content=${page.content}")
        }
        refreshAllCourses()
    }

    private fun notifyEnrolledUsers(course: Course, lessonTitle: String) {
        if (course.enrolledUserIds.isEmpty()) {
            logInfo("No enrolled users for course ${course.title}")
            return
        }
        course.enrolledUserIds.forEach { userId ->
            viewModelScope.launch {
                try {
                    val user = userDao.getUserById(userId)?.toUser()
                    logInfo("Checking user for ID $userId: $user")
                    user?.let {
                        logInfo("Sending email to ${it.email} for new lesson: $lessonTitle")
                        sendEmail(it.email, "New Lesson: $lessonTitle", "A new lesson has been added to your enrolled course: ${course.title}.")
                    } ?: logError("User $userId not found")
                } catch (e: Exception) {
                    logError("Failed to notify user $userId: ${e.message}")
                }
            }
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
                logInfo("✅ Starting email client for $to with chooser")
            } else {
                logError("❌ No email client found for ACTION_SEND")
            }
        } catch (e: Exception) {
            logError("❌ Failed to start email client for $to: ${e.message}")
        }
    }

    suspend fun markLessonCompleted(courseId: String, lessonId: String): Result<Unit> {
        return try {
            val userId = _currentUser.value?.id ?: run {
                logError("No current user to mark lesson as completed")
                return Result.failure(Exception("No current user"))
            }
            val lessonEntity = lessonDao.getLessonById(lessonId) ?: run {
                logError("Lesson $lessonId not found")
                return Result.failure(Exception("Lesson $lessonId not found"))
            }

            logInfo("Before update: Lesson $lessonId, completedByUserIds: ${lessonEntity.completedByUserIds}")
            if (!lessonEntity.completedByUserIds.contains(userId)) {
                val updatedCompletedByUserIds = lessonEntity.completedByUserIds.toMutableList().apply {
                    add(userId)
                }
                val updatedLesson = lessonEntity.copy(completedByUserIds = updatedCompletedByUserIds)
                lessonDao.updateLesson(updatedLesson)
                val updated = lessonDao.getLessonById(lessonId)
                logInfo("After update: Lesson $lessonId, completedByUserIds: ${updated?.completedByUserIds}")
                refreshAllCourses()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            logError("Failed to mark lesson $lessonId as completed: ${e.message}")
            Result.failure(e)
        }
    }

    fun enrollInCourse(courseId: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            val userId = _currentUser.value?.id ?: run {
                logError("No current user to enroll in course")
                onResult(Result.failure(Exception("No current user")))
                return@launch
            }
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            if (!course.pendingUserIds.contains(userId) && !course.enrolledUserIds.contains(userId)) {
                val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds + userId).toMutableList())
                courseDao.updateCourse(updatedCourse.toEntity())
                refreshAllCourses()
                logInfo("User $userId enrolled in course $courseId")
            }
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to enroll in course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun unenrollFromCourse(courseId: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            val userId = _currentUser.value?.id ?: run {
                logError("No current user to unenroll from course")
                onResult(Result.failure(Exception("No current user")))
                return@launch
            }
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            val updatedCourse = course.copy(enrolledUserIds = (course.enrolledUserIds - userId).toMutableList())
            courseDao.updateCourse(updatedCourse.toEntity())
            refreshAllCourses()
            logInfo("User $userId unenrolled from course $courseId")
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to unenroll from course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun isEnrolled(courseId: String): Boolean {
        val userId = _currentUser.value?.id ?: return false
        return getCourseById(courseId)?.enrolledUserIds?.contains(userId) == true
    }

    suspend fun getPendingRequests(courseId: String): List<User> {
        val course = getCourseById(courseId)
        return course?.pendingUserIds?.mapNotNull { userDao.getUserById(it)?.toUser() } ?: emptyList()
    }

    fun acceptEnrollment(courseId: String, userId: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            logInfo("Accepting enrollment for course $courseId, user $userId")
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            val updatedCourse = course.copy(
                pendingUserIds = (course.pendingUserIds - userId).toMutableList(),
                enrolledUserIds = (course.enrolledUserIds + userId).toMutableList()
            )
            logInfo("Updating course: pendingUserIds=${updatedCourse.pendingUserIds}, enrolledUserIds=${updatedCourse.enrolledUserIds}")
            courseDao.updateCourse(updatedCourse.toEntity())
            refreshAllCourses()
            logInfo("Course $courseId updated and refreshed")
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to accept enrollment for course $courseId, user $userId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun rejectEnrollment(courseId: String, userId: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            logInfo("Rejecting enrollment for course $courseId, user $userId")
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            val updatedCourse = course.copy(pendingUserIds = (course.pendingUserIds - userId).toMutableList())
            logInfo("Updating course: pendingUserIds=${updatedCourse.pendingUserIds}")
            courseDao.updateCourse(updatedCourse.toEntity())
            refreshAllCourses()
            logInfo("Course $courseId updated and refreshed")
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to reject enrollment for course $courseId, user $userId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    suspend fun getUserById(userId: String): User? {
        return try {
            userDao.getUserById(userId)?.toUser()
        } catch (e: Exception) {
            logError("Failed to get user $userId: ${e.message}")
            null
        }
    }

    fun canAccessLesson(lessons: List<Lesson>, index: Int, userId: String?): Boolean {
        if (userId == null) return false
        return index == 0 || lessons[index - 1].completedByUserIds.contains(userId)
    }

    fun addLessonPage(lessonId: String, type: PageType, content: String, onResult: (Result<LessonPageEntity>) -> Unit) = viewModelScope.launch {
        try {
            val page = LessonPageEntity(
                id = UUID.randomUUID().toString(),
                lessonId = lessonId,
                type = type,
                content = content
            )
            lessonPageDao.insertPage(page)
            logInfo("Added lesson page to lesson $lessonId: Type=$type, Content=$content")
            onResult(Result.success(page))
        } catch (e: Exception) {
            logError("Failed to add lesson page to lesson $lessonId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun updateLessonPage(page: LessonPageEntity, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            lessonPageDao.updatePage(page)
            logInfo("Updated lesson page ${page.id}")
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to update lesson page ${page.id}: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun deleteLessonPage(page: LessonPageEntity, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            lessonPageDao.deletePage(page)
            logInfo("Deleted lesson page ${page.id}")
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to delete lesson page ${page.id}: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    suspend fun getLessonPages(lessonId: String): List<LessonPageEntity> {
        return try {
            lessonPageDao.getPagesForLesson(lessonId) ?: emptyList()
        } catch (e: Exception) {
            logError("Failed to get lesson pages for lesson $lessonId: ${e.message}")
            emptyList()
        }
    }

    data class QuizResult(val studentName: String, val score: Int, val total: Int)

    suspend fun getQuizResults(courseId: String): List<QuizResult> {
        try {
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found for quiz results")
                return emptyList()
            }
            val quiz = course.quiz ?: run {
                logError("No quiz found for course $courseId")
                return emptyList()
            }
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
            }.also {
                logInfo("Retrieved ${it.size} quiz results for course $courseId")
            }
        } catch (e: Exception) {
            logError("Failed to get quiz results for course $courseId: ${e.message}")
            return emptyList()
        }
    }

    fun submitQuizResult(courseId: String, userId: String, score: Int, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            val quiz = quizDao.getQuizForCourse(courseId)
            if (quiz == null) {
                logError("Quiz for course $courseId not found")
                onResult(Result.failure(Exception("Quiz not found")))
                return@launch
            }
            val questions = quizQuestionDao.getQuestionsForQuiz(quiz.id)
            if (score < 0 || score > questions.size) {
                logError("Invalid score $score for quiz with ${questions.size} questions")
                onResult(Result.failure(Exception("Invalid score: $score")))
                return@launch
            }
            val submission = QuizSubmissionEntity(
                id = UUID.randomUUID().toString(),
                courseId = courseId,
                userId = userId,
                score = score
            )
            quizSubmissionDao.updateSubmission(courseId, userId, submission)
            logInfo("Submitted quiz result for course $courseId, user $userId: Score=$score")
            refreshAllCourses()
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to submit quiz result for course $courseId, user $userId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun addQuizToCourse(courseId: String, questions: List<QuizQuestion>, onResult: (Result<QuizEntity>) -> Unit) = viewModelScope.launch {
        try {
            // Validate quiz questions
            val validationError = validateQuizInput(questions)
            if (validationError != null) {
                logError("Validation failed for quiz: $validationError")
                onResult(Result.failure(Exception(validationError)))
                return@launch
            }

            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }

            // Delete existing quiz if present
            val existingQuiz = quizDao.getQuizForCourse(courseId)
            if (existingQuiz != null) {
                logInfo("Quiz already exists for course $courseId, deleting old quiz")
                quizDao.deleteQuizAndQuestions(existingQuiz, quizQuestionDao)
            }

            // Create new quiz, unpublished by default
            val quizId = UUID.randomUUID().toString()
            val quizEntity = QuizEntity(
                id = quizId,
                courseId = courseId,
                isPublished = false
            )
            insertQuizWithQuestions(quizEntity, questions)
            logInfo("Quiz added to course $courseId successfully")
            onResult(Result.success(quizEntity))
        } catch (e: Exception) {
            logError("Failed to add quiz to course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun publishQuiz(courseId: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            val quiz = quizDao.getQuizForCourse(courseId)
            if (quiz == null) {
                logError("Quiz for course $courseId not found")
                onResult(Result.failure(Exception("Quiz not found")))
                return@launch
            }
            val updatedQuiz = quiz.copy(isPublished = true)
            quizDao.updateQuiz(updatedQuiz)
            logInfo("Quiz published for course $courseId")
            refreshAllCourses()
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to publish quiz for course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    fun unpublishQuiz(courseId: String, onResult: (Result<Unit>) -> Unit) = viewModelScope.launch {
        try {
            val course = getCourseById(courseId) ?: run {
                logError("Course $courseId not found")
                onResult(Result.failure(Exception("Course $courseId not found")))
                return@launch
            }
            val quiz = quizDao.getQuizForCourse(courseId)
            if (quiz == null) {
                logError("Quiz for course $courseId not found")
                onResult(Result.failure(Exception("Quiz not found")))
                return@launch
            }
            val updatedQuiz = quiz.copy(isPublished = false)
            quizDao.updateQuiz(updatedQuiz)
            logInfo("Quiz unpublished for course $courseId")
            refreshAllCourses()
            onResult(Result.success(Unit))
        } catch (e: Exception) {
            logError("Failed to unpublish quiz for course $courseId: ${e.message}")
            onResult(Result.failure(e))
        }
    }

    private fun validateQuizInput(questions: List<QuizQuestion>): String? {
        if (questions.isEmpty()) {
            return "Questions list cannot be empty"
        }
        questions.forEachIndexed { index, question ->
            if (question.question.isBlank()) {
                return "Question text cannot be blank at index $index"
            }
            // Allow 2 options for true/false questions, 4 options for multiple-choice
            if (question.options.size != 2 && question.options.size != 4) {
                return "Question at index $index must have exactly 2 or 4 options, but found ${question.options.size}"
            }
            if (question.options.any { it.isBlank() }) {
                return "Options cannot be blank at index $index"
            }
            if (question.options.distinct().size != question.options.size) {
                return "Options must be unique at index $index"
            }
        }
        return null
    }

    private suspend fun insertQuizWithQuestions(quiz: QuizEntity, questions: List<QuizQuestion>) {
        quizDao.insertQuiz(quiz)
        logInfo("Inserted quiz for course ${quiz.courseId} with ID: ${quiz.id}")
        val questionEntities = questions.map { it.toEntity(quiz.id) }
        quizQuestionDao.insertAllWithValidation(questionEntities)
        logInfo("Inserted ${questionEntities.size} questions for quiz ${quiz.id}")
        refreshAllCourses()
    }

    suspend fun debugCourse(courseId: String) {
        try {
            val course = db.courseDao().getCourseById(courseId)
            logInfo("Course: $course")
        } catch (e: Exception) {
            logError("Failed to debug course $courseId: ${e.message}")
        }
    }

    suspend fun debugUser(userId: String) {
        try {
            val user = db.userDao().getUserById(userId)
            logInfo("User: $user")
        } catch (e: Exception) {
            logError("Failed to debug user $userId: ${e.message}")
        }
    }

    suspend fun deleteTestUser(email: String) {
        try {
            val user = db.userDao().getUserByEmail(email)
            user?.let { userDao.deleteUser(it) }
            logInfo("Deleted test user: $email")
        } catch (e: Exception) {
            logError("Failed to delete test user $email: ${e.message}")
        }
    }

    // Helper methods for logging
    private fun logInfo(message: String) {
        println("INFO: $message")
    }

    private fun logError(message: String) {
        println("ERROR: $message")
    }
}