package com.example.personaltutorapp.ui.screens

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
import com.example.personaltutorapp.model.User
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    val course by remember(allCourses, courseId) {
        derivedStateOf { allCourses.find { it.id == courseId } }
    }
    val pendingUsers by produceState(initialValue = emptyList<User>(), courseId, allCourses) {
        value = viewModel.getPendingRequests(courseId)
    }
    var isLoading by remember { mutableStateOf(true) }
    val loadingUsers = remember { mutableStateMapOf<String, Boolean>() }
    val coroutineScope = rememberCoroutineScope()
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(course) {
        isLoading = false
    }

    LaunchedEffect(pendingUsers) {
        println("Pending users updated: ${pendingUsers.map { it.displayName }}")
    }

    Surface(modifier = Modifier.fillMaxSize()) {
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
        } else if (course == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Course not found",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.semantics {
                        testTag = "course_not_found"
                        contentDescription = "Course not found"
                    }
                )
            }
        } else {
            course?.let { course ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "course_header"
                                contentDescription = "Course details"
                            },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = course.title,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.semantics {
                                    testTag = "course_title"
                                    contentDescription = "Course title: ${course.title}"
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Subject: ${course.subject}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.semantics {
                                    testTag = "course_subject"
                                    contentDescription = "Course subject: ${course.subject}"
                                }
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = course.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.semantics {
                                    testTag = "course_description"
                                    contentDescription = "Course description"
                                }
                            )
                        }
                    }

                    if (currentUser?.id == course.tutor.id) {
                        Button(
                            onClick = { navController.navigate(NavRoutes.AddLesson.createRoute(courseId)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics {
                                    testTag = "add_lesson_button"
                                    contentDescription = "Add lesson"
                                },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Add Lesson")
                        }

                        Button(
                            onClick = { navController.navigate(NavRoutes.QuizResults.createRoute(courseId)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics {
                                    testTag = "quiz_results_button"
                                    contentDescription = "View quiz results"
                                },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("View Quiz Results")
                        }

                        if (pendingUsers.isNotEmpty()) {
                            Text(
                                text = "Pending Enrolment Requests",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics {
                                    testTag = "pending_requests_title"
                                    contentDescription = "Pending enrolment requests"
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            pendingUsers.forEach { user ->
                                ElevatedCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .semantics {
                                            testTag = "pending_user_${user.id}"
                                            contentDescription = "Pending enrolment request for ${user.displayName}"
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
                                            text = user.displayName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier
                                                .weight(1f)
                                                .semantics {
                                                    testTag = "user_name_${user.id}"
                                                    contentDescription = "User: ${user.displayName}"
                                                }
                                        )
                                        Row {
                                            TextButton(
                                                onClick = {
                                                    loadingUsers[user.id] = true
                                                    coroutineScope.launch {
                                                        viewModel.acceptEnrollment(courseId, user.id) { result ->
                                                            loadingUsers[user.id] = false
                                                            result.onSuccess {
                                                                snackbarMessage = "Request from ${user.displayName} accepted"
                                                                println("Accepted enrolment for user ${user.id} in course $courseId")
                                                            }.onFailure { e ->
                                                                snackbarMessage = "Failed to accept request: ${e.message}"
                                                                println("Failed to accept enrolment for user ${user.id}: ${e.message}")
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.semantics {
                                                    testTag = "accept_button_${user.id}"
                                                    contentDescription = "Accept request for ${user.displayName}"
                                                },
                                                enabled = loadingUsers[user.id] != true
                                            ) {
                                                if (loadingUsers[user.id] == true) {
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
                                                    loadingUsers[user.id] = true
                                                    coroutineScope.launch {
                                                        viewModel.rejectEnrollment(courseId, user.id) { result ->
                                                            loadingUsers[user.id] = false
                                                            result.onSuccess {
                                                                snackbarMessage = "Request from ${user.displayName} rejected"
                                                                println("Rejected enrolment for user ${user.id} in course $courseId")
                                                            }.onFailure { e ->
                                                                snackbarMessage = "Failed to reject request: ${e.message}"
                                                                println("Failed to reject enrolment for user ${user.id}: ${e.message}")
                                                            }
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.semantics {
                                                    testTag = "reject_button_${user.id}"
                                                    contentDescription = "Reject request for ${user.displayName}"
                                                },
                                                enabled = loadingUsers[user.id] != true
                                            ) {
                                                if (loadingUsers[user.id] == true) {
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
                    } else {
                        val isEnrolled = course.enrolledUserIds.contains(currentUser?.id)
                        val isPending = course.pendingUserIds.contains(currentUser?.id)

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    if (isEnrolled) {
                                        viewModel.unenrollFromCourse(courseId) { result ->
                                            result.onSuccess {
                                                snackbarMessage = "Unenrolled from ${course.title}"
                                                println("Unenrolled user ${currentUser?.id} from course $courseId")
                                            }.onFailure { e ->
                                                snackbarMessage = "Failed to unenrol: ${e.message}"
                                                println("Failed to unenrol user ${currentUser?.id}: ${e.message}")
                                            }
                                        }
                                    } else if (!isPending) {
                                        viewModel.enrollInCourse(courseId) { result ->
                                            result.onSuccess {
                                                snackbarMessage = "Enrolment request sent for ${course.title}"
                                                println("Requested enrolment for user ${currentUser?.id} in course $courseId")
                                            }.onFailure { e ->
                                                snackbarMessage = "Failed to request enrolment: ${e.message}"
                                                println("Failed to request enrolment for user ${currentUser?.id}: ${e.message}")
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics {
                                    testTag = "enrollment_button"
                                    contentDescription = when {
                                        isEnrolled -> "Unenrol from ${course.title}"
                                        isPending -> "Enrolment pending for ${course.title}"
                                        else -> "Request enrolment in ${course.title}"
                                    }
                                },
                            shape = MaterialTheme.shapes.medium,
                            enabled = !isPending
                        ) {
                            Text(
                                when {
                                    isEnrolled -> "Unenrol from Course"
                                    isPending -> "Enrolment Pending"
                                    else -> "Request Enrolment"
                                }
                            )
                        }
                    }

                    Text(
                        text = "Lessons",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics {
                            testTag = "lessons_title"
                            contentDescription = "Lessons list"
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (course.lessons.isEmpty()) {
                        Text(
                            text = "No lessons available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "no_lessons_text"
                                contentDescription = "No lessons available"
                            }
                        )
                    } else {
                        course.lessons.forEachIndexed { index, lesson ->
                            val completed = lesson.completedByUserIds.contains(currentUser?.id)
                            val unlocked = viewModel.canAccessLesson(course.lessons, index, currentUser?.id)

                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(enabled = unlocked) {
                                        navController.navigate(NavRoutes.LessonDetail.createRoute(courseId, lesson.id))
                                    }
                                    .semantics {
                                        testTag = "lesson_${lesson.id}"
                                        contentDescription = "Lesson: ${lesson.title}, ${if (completed) "completed" else if (!unlocked) "locked" else "unlocked"}"
                                    },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.elevatedCardColors(
                                    containerColor = if (unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = lesson.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics {
                                                testTag = "lesson_title_${lesson.id}"
                                                contentDescription = "Lesson title: ${lesson.title}"
                                            }
                                    )
                                    Text(
                                        text = when {
                                            completed -> "✅"
                                            !unlocked -> "🔒"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.semantics {
                                            testTag = "lesson_status_${lesson.id}"
                                            contentDescription = if (completed) "Completed" else if (!unlocked) "Locked" else "Unlocked"
                                        }
                                    )
                                }
                            }
                        }
                    }

                    snackbarMessage?.let { message ->
                        LaunchedEffect(message) {
                            coroutineScope.launch {
                                kotlinx.coroutines.delay(3000)
                                snackbarMessage = null
                            }
                        }
                        Snackbar(
                            modifier = Modifier.padding(16.dp),
                            action = {
                                TextButton(onClick = { snackbarMessage = null }) {
                                    Text("Dismiss")
                                }
                            }
                        ) {
                            Text(message)
                        }
                    }
                }
            }
        }
    }
}