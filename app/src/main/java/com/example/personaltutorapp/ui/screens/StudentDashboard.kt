package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun StudentDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Refresh courses when the dashboard is loaded
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            viewModel.refreshAllCourses()
            isLoading = false
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 显示用户头像
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

            if (isLoading) {
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
                        onTakeQuizClick = {
                            if (course.quiz != null && course.quiz.isPublished) {
                                navController.navigate(NavRoutes.TakeQuiz.createRoute(course.id))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun CourseCardWithStatus(
    course: Course,
    currentUserId: String?,
    onClick: () -> Unit,
    onTakeQuizClick: () -> Unit
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
                        Spacer(modifier = Modifier.height(8.dp))
                        // Show quiz status and button to take quiz if published
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