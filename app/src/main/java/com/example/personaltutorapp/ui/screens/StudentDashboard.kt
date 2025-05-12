package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.model.BookingEntity
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun StudentDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val bookings by viewModel.studentBookings.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isLoadingCourses by remember { mutableStateOf(true) }
    var isLoadingBookings by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }
    var bookingError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(currentUser?.id) {
        if (currentUser?.id == null) {
            isLoadingCourses = false
            isLoadingBookings = false
            bookingError = null
            return@LaunchedEffect
        }
        isLoadingCourses = true
        isLoadingBookings = true
        try {
            currentUser?.id?.let { studentId ->
                viewModel.loadStudentBookings(studentId)
                viewModel.refreshAllCourses() // 合并第一个版本的刷新逻辑
                bookingError = null
            }
        } catch (e: Exception) {
            bookingError = "Failed to load bookings: ${e.message}"
            println("Failed to load bookings: ${e.message}")
        } finally {
            isLoadingCourses = false
            isLoadingBookings = false
        }
    }

    val filteredCourses = allCourses.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.subject.contains(searchQuery, ignoreCase = true)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                currentUser?.profileImageUrl?.let { url ->
                    val painter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(url)
                            .size(48, 48)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .allowHardware(false)
                            .build(),
                        placeholder = painterResource(id = android.R.drawable.ic_menu_gallery),
                        error = painterResource(id = android.R.drawable.ic_menu_report_image)
                    )
                    Image(
                        painter = painter,
                        contentDescription = "User profile image",
                        modifier = Modifier
                            .size(48.dp)
                            .graphicsLayer {
                                clip = true
                                shape = CircleShape
                            }
                            .semantics {
                                testTag = "profile_image"
                            }
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = "Welcome, ${currentUser?.displayName ?: "Student"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        testTag = "welcome_text"
                        contentDescription = "Welcome message"
                    }
                )
            }

            // 保留第二个版本的 "Book a Meeting with a Tutor" 按钮
            Button(
                onClick = {
                    val studentId = currentUser?.id
                    if (studentId == null) {
                        snackbarMessage = "Failed to book meeting: User not logged in"
                        return@Button
                    }
                    // 直接使用测试导师的 email 获取 tutorId
                    coroutineScope.launch {
                        viewModel.fetchUserByEmail("test_tutor@example.com")
                        val tutor = viewModel.userByEmail.value
                        val tutorId = tutor?.id
                        if (tutorId != null) {
                            navController.navigate(NavRoutes.StudentBooking.createRoute(tutorId, studentId))
                        } else {
                            snackbarMessage = "No tutor available for booking"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "book_meeting_button"
                        contentDescription = "Book a meeting with a tutor"
                    },
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Book a Meeting with a Tutor")
            }

            // 保留第二个版本的 "Your Bookings" 部分
            Text(
                text = "Your Bookings",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics {
                    testTag = "bookings_title"
                    contentDescription = "Your bookings"
                }
            )

            if (isLoadingBookings) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            testTag = "loading_bookings_indicator"
                            contentDescription = "Loading bookings"
                        }
                    )
                }
            } else if (bookingError != null) {
                Text(
                    text = bookingError ?: "Error loading bookings",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics {
                        testTag = "booking_error_text"
                        contentDescription = "Booking error message"
                    }
                )
            } else if (bookings.isEmpty()) {
                Text(
                    text = "No bookings scheduled",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        testTag = "no_bookings_text"
                        contentDescription = "No bookings scheduled"
                    }
                )
            } else {
                LazyColumn {
                    items(bookings) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancel = {
                                coroutineScope.launch {
                                    viewModel.cancelBooking(booking) { result ->
                                        result.onSuccess {
                                            snackbarMessage = "Booking cancelled successfully"
                                        }.onFailure { e ->
                                            snackbarMessage = "Failed to cancel booking: ${e.message}"
                                        }
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or subject") },
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        testTag = "search_field"
                        contentDescription = "Search courses"
                    }
            )

            Text(
                text = "Available Courses",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics {
                    testTag = "courses_title"
                    contentDescription = "Available courses"
                }
            )

            if (isLoadingCourses) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            testTag = "loading_indicator"
                            contentDescription = "Loading"
                        }
                    )
                }
            } else if (filteredCourses.isEmpty()) {
                Text(
                    text = "No courses found",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        testTag = "no_courses_text"
                        contentDescription = "No courses found"
                    }
                )
            } else {
                filteredCourses.forEach { course ->
                    CourseCardWithStatus(
                        course = course,
                        currentUserId = currentUser?.id,
                        onClick = {
                            navController.navigate(NavRoutes.CourseDetail.createRoute(course.id))
                        },
                        // 保留第一个版本的 Quiz 相关逻辑
                        onTakeQuizClick = {
                            if (course.quiz != null && course.quiz.isPublished) {
                                navController.navigate(NavRoutes.TakeQuiz.createRoute(course.id))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 保留第二个版本的 snackbar 提示
            snackbarMessage?.let { message ->
                Snackbar(
                    modifier = Modifier.padding(top = 8.dp),
                    action = {
                        TextButton(onClick = { snackbarMessage = null }) { Text("Dismiss") }
                    }
                ) { Text(message) }
            }
        }
    }
}

@Composable
fun CourseCardWithStatus(
    course: Course,
    currentUserId: String?,
    onClick: () -> Unit,
    onTakeQuizClick: () -> Unit // 保留第一个版本的 Quiz 相关参数
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .semantics {
                testTag = "course_card_${course.id}"
                contentDescription = "Course: ${course.title}"
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = course.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "course_title_${course.id}"
                    contentDescription = "Course title: ${course.title}"
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = course.subject,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.semantics {
                    testTag = "course_subject_${course.id}"
                    contentDescription = "Course subject: ${course.subject}"
                }
            )

            currentUserId?.let { userId ->
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    course.enrolledUserIds.contains(userId) -> {
                        Text(
                            text = "✅ Enrolled",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "enrolled_status_${course.id}"
                                contentDescription = "Enrolled status"
                            }
                        )
                        // 保留第一个版本的 Quiz 相关 UI 和逻辑
                        Spacer(modifier = Modifier.height(8.dp))
                        if (course.quiz != null) {
                            Text(
                                text = if (course.quiz.isPublished) "Quiz Available" else "Quiz Not Published",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.semantics {
                                    testTag = "quiz_status_${course.id}"
                                    contentDescription = if (course.quiz.isPublished) "Quiz available" else "Quiz not published"
                                }
                            )
                            if (course.quiz.isPublished) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = onTakeQuizClick,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .semantics {
                                            testTag = "take_quiz_button_${course.id}"
                                            contentDescription = "Take quiz for ${course.title}"
                                        },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("Take Quiz")
                                }
                            }
                        } else {
                            Text(
                                text = "No Quiz Available",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.semantics {
                                    testTag = "no_quiz_status_${course.id}"
                                    contentDescription = "No quiz available"
                                }
                            )
                        }
                    }
                    course.pendingUserIds.contains(userId) -> {
                        Text(
                            text = "⌛ Enrolment Pending",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "pending_status_${course.id}"
                                contentDescription = "Enrolment pending status"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookingCard(
    booking: BookingEntity,
    onCancel: () -> Unit
) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                testTag = "booking_card_${booking.id}"
                contentDescription = "Booking from ${dateFormat.format(booking.startTime)} to ${dateFormat.format(booking.endTime)}"
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Start: ${dateFormat.format(booking.startTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        testTag = "booking_start_${booking.id}"
                        contentDescription = "Start time: ${dateFormat.format(booking.startTime)}"
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "End: ${dateFormat.format(booking.endTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        testTag = "booking_end_${booking.id}"
                        contentDescription = "End time: ${dateFormat.format(booking.endTime)}"
                    }
                )
            }
            TextButton(
                onClick = onCancel,
                modifier = Modifier.semantics {
                    testTag = "cancel_booking_button_${booking.id}"
                    contentDescription = "Cancel booking"
                }
            ) {
                Text("Cancel")
            }
        }
    }
}