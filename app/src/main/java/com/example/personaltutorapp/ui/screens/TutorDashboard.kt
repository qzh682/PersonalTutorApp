package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.model.User
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape

@Composable
fun TutorDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val courses = viewModel.getCoursesForCurrentUser()
    val pendingUsersMap = remember { mutableStateMapOf<String, List<User>>() }
    val loadingUsers = remember { mutableStateMapOf<String, Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    // Load pending users for each course
    LaunchedEffect(courses) {
        isLoading = true
        try {
            courses.forEach { course ->
                coroutineScope.launch {
                    val users = course.pendingUserIds.mapNotNull { userId ->
                        viewModel.getUserById(userId)
                    }
                    pendingUsersMap[course.id] = users
                    println("Loaded ${users.size} pending users for course ${course.id}")
                }
            }
        } catch (e: Exception) {
            snackbarMessage = "Failed to load pending requests: ${e.message}"
            println("Failed to load pending requests: ${e.message}")
        } finally {
            isLoading = false
        }
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
                            .size(48, 48) // 头像大小
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

                currentUser?.let { user ->
                    Text(
                        text = "Welcome, ${user.displayName}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            testTag = "welcome_text"
                            contentDescription = "Welcome message for ${user.displayName}"
                        }
                    )
                } ?: Text(
                    text = "Welcome, Tutor",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        testTag = "welcome_text"
                        contentDescription = "Welcome message"
                    }
                )
            }

            Button(
                onClick = { navController.navigate(NavRoutes.CreateCourse.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "create_course_button"
                        contentDescription = "Create new course"
                    },
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Create New Course")
            }

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
            } else if (courses.isEmpty()) {
                Text(
                    text = "No courses available",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        testTag = "no_courses_text"
                        contentDescription = "No courses available"
                    }
                )
            } else {
                courses.forEach { course ->
                    if (course.id.isNotBlank()) {
                        CourseCard(course = course) {
                            navController.navigate(NavRoutes.CourseDetail.createRoute(course.id))
                        }

                        val totalLessons = course.lessons.size
                        val enrolledCount = course.enrolledUserIds.size
                        val avgProgress = if (totalLessons > 0 && enrolledCount > 0) {
                            val totalCompleted = course.enrolledUserIds.sumOf { userId ->
                                course.lessons.count { lesson -> userId in lesson.completedByUserIds }
                            }
                            (totalCompleted.toFloat() / (totalLessons * enrolledCount) * 100).toInt()
                        } else 0

                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .semantics {
                                    testTag = "stats_card_${course.id}"
                                    contentDescription = "Course statistics for ${course.title}"
                                },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Enrolled Students: $enrolledCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.semantics {
                                        testTag = "enrolled_count_${course.id}"
                                        contentDescription = "Enrolled students: $enrolledCount"
                                    }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Average Progress: $avgProgress%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.semantics {
                                        testTag = "progress_${course.id}"
                                        contentDescription = "Average progress: $avgProgress percent"
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ProgressBar(
                                    percentage = avgProgress,
                                    modifier = Modifier.semantics {
                                        testTag = "progress_bar_${course.id}"
                                        contentDescription = "Progress bar: $avgProgress percent"
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Display quiz status
                                Text(
                                    text = when {
                                        course.quiz == null -> "No Quiz Available"
                                        course.quiz.isPublished -> "Quiz Published"
                                        else -> "Quiz Not Published"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.semantics {
                                        testTag = "quiz_status_${course.id}"
                                        contentDescription = when {
                                            course.quiz == null -> "No quiz available"
                                            course.quiz.isPublished -> "Quiz published"
                                            else -> "Quiz not published"
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (course.quiz == null) {
                                        Button(
                                            onClick = {
                                                navController.navigate(NavRoutes.AddQuiz.createRoute(course.id))
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .semantics {
                                                    testTag = "add_quiz_button_${course.id}"
                                                    contentDescription = "Add quiz for ${course.title}"
                                                },
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text("Add Quiz")
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                coroutineScope.launch {
                                                    if (course.quiz.isPublished) {
                                                        viewModel.unpublishQuiz(course.id) { result ->
                                                            result.onSuccess {
                                                                snackbarMessage = "Quiz unpublished successfully"
                                                                println("Quiz unpublished for course ${course.id}")
                                                            }.onFailure { e ->
                                                                snackbarMessage = "Failed to unpublish quiz: ${e.message}"
                                                                println("Failed to unpublish quiz: ${e.message}")
                                                            }
                                                        }
                                                    } else {
                                                        viewModel.publishQuiz(course.id) { result ->
                                                            result.onSuccess {
                                                                snackbarMessage = "Quiz published successfully"
                                                                println("Quiz published for course ${course.id}")
                                                            }.onFailure { e ->
                                                                snackbarMessage = "Failed to publish quiz: ${e.message}"
                                                                println("Failed to publish quiz: ${e.message}")
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(48.dp)
                                                .semantics {
                                                    testTag = "publish_quiz_button_${course.id}"
                                                    contentDescription = if (course.quiz.isPublished) "Unpublish quiz" else "Publish quiz"
                                                },
                                            shape = MaterialTheme.shapes.medium
                                        ) {
                                            Text(if (course.quiz.isPublished) "Unpublish" else "Publish")
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            navController.navigate(NavRoutes.QuizResults.createRoute(course.id))
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics {
                                                testTag = "quiz_results_button_${course.id}"
                                                contentDescription = "View quiz results for ${course.title}"
                                            },
                                        shape = MaterialTheme.shapes.medium,
                                        enabled = course.quiz != null
                                    ) {
                                        Text("View Quiz Results")
                                    }
                                }
                            }
                        }

                        val pendingUsers = pendingUsersMap[course.id] ?: emptyList()
                        if (pendingUsers.isNotEmpty()) {
                            Text(
                                text = "Pending Requests",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.semantics {
                                    testTag = "pending_requests_title_${course.id}"
                                    contentDescription = "Pending requests for ${course.title}"
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            pendingUsers.forEach { pendingUser ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .semantics {
                                            testTag = "pending_user_${pendingUser.id}_${course.id}"
                                            contentDescription = "Pending request for ${pendingUser.displayName}"
                                        },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = pendingUser.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .weight(1f)
                                                .semantics {
                                                    testTag = "pending_user_name_${pendingUser.id}_${course.id}"
                                                    contentDescription = "User: ${pendingUser.displayName}"
                                                }
                                        )
                                        Row {
                                            TextButton(
                                                onClick = {
                                                    loadingUsers[pendingUser.id] = true
                                                    coroutineScope.launch {
                                                        viewModel.acceptEnrollment(course.id, pendingUser.id) { result ->
                                                            loadingUsers[pendingUser.id] = false
                                                            result.onSuccess {
                                                                snackbarMessage = "Request from ${pendingUser.displayName} accepted"
                                                                println("Accepted enrolment for user ${pendingUser.id} in course ${course.id}")
                                                            }.onFailure { e ->
                                                                snackbarMessage = "Failed to accept request: ${e.message}"
                                                                println("Failed to accept enrolment for user ${pendingUser.id}: ${e.message}")
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.semantics {
                                                    testTag = "accept_button_${pendingUser.id}_${course.id}"
                                                    contentDescription = "Accept request for ${pendingUser.displayName}"
                                                },
                                                enabled = loadingUsers[pendingUser.id] != true
                                            ) {
                                                if (loadingUsers[pendingUser.id] == true) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Text("Accept")
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextButton(
                                                onClick = {
                                                    loadingUsers[pendingUser.id] = true
                                                    coroutineScope.launch {
                                                        viewModel.rejectEnrollment(course.id, pendingUser.id) { result ->
                                                            loadingUsers[pendingUser.id] = false
                                                            result.onSuccess {
                                                                snackbarMessage = "Request from ${pendingUser.displayName} rejected"
                                                                println("Rejected enrolment for user ${pendingUser.id} in course ${course.id}")
                                                            }.onFailure { e ->
                                                                snackbarMessage = "Failed to reject request: ${e.message}"
                                                                println("Failed to reject enrolment for user ${pendingUser.id}: ${e.message}")
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.semantics {
                                                    testTag = "reject_button_${pendingUser.id}_${course.id}"
                                                    contentDescription = "Reject request for ${pendingUser.displayName}"
                                                },
                                                enabled = loadingUsers[pendingUser.id] != true
                                            ) {
                                                if (loadingUsers[pendingUser.id] == true) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                } else {
                                                    Text("Reject")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

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
fun CourseCard(course: Course, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
        }
    }
}

@Composable
fun ProgressBar(percentage: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .semantics {
                contentDescription = "Progress bar: $percentage percent"
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentage / 100f)
                .height(12.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}