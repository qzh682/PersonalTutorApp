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

@Composable
fun TutorDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val courses = viewModel.getCoursesForCurrentUser()
    val pendingUsersMap = remember { mutableStateMapOf<String, List<User>>() }
    val coroutineScope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ✅ 异步加载每门课程的 pending users
    LaunchedEffect(courses) {
        isLoading = true
        try {
            courses.forEach { course ->
                coroutineScope.launch {
                    val users = course.pendingUserIds.mapNotNull { userId ->
                        viewModel.getUserById(userId)
                    }
                    pendingUsersMap[course.id] = users
                }
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load pending requests"
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
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        navController.navigate(NavRoutes.QuizResults.createRoute(course.id))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .semantics {
                                            testTag = "quiz_results_button_${course.id}"
                                            contentDescription = "View quiz results for ${course.title}"
                                        },
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text("View Quiz Results")
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
                                                    viewModel.acceptEnrollment(course.id, pendingUser.id)
                                                },
                                                modifier = Modifier.semantics {
                                                    testTag = "accept_button_${pendingUser.id}_${course.id}"
                                                    contentDescription = "Accept request for ${pendingUser.displayName}"
                                                }
                                            ) {
                                                Text("Accept")
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            TextButton(
                                                onClick = {
                                                    viewModel.rejectEnrollment(course.id, pendingUser.id)
                                                },
                                                modifier = Modifier.semantics {
                                                    testTag = "reject_button_${pendingUser.id}_${course.id}"
                                                    contentDescription = "Reject request for ${pendingUser.displayName}"
                                                }
                                            ) {
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

            errorMessage?.let {
                Snackbar(
                    modifier = Modifier.padding(top = 8.dp),
                    action = {
                        TextButton(onClick = { errorMessage = null }) { Text("Dismiss") }
                    }
                ) { Text(it) }
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