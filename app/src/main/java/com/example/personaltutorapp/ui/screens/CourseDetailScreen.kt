package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.viewmodel.MainViewModel
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.model.User

@Composable
fun CourseDetailScreen(
    courseId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser = viewModel.currentUser.collectAsState().value
    val course = remember { viewModel.getCourseById(courseId) }

    val pendingUsers = remember { mutableStateOf<List<User>>(emptyList()) }

    LaunchedEffect(courseId) {
        viewModel.getPendingRequests(courseId)?.let {
            pendingUsers.value = it
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (course == null) {
                Text("Course not found or not yet loaded", style = MaterialTheme.typography.titleMedium)
                return@Column
            }

            Text(text = course.title, style = MaterialTheme.typography.headlineLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Subject: ${course.subject}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = course.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))

            if (currentUser?.id == course.tutor.id) {
                Button(
                    onClick = {
                        navController.navigate("${NavRoutes.AddLesson.route}/$courseId")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Lesson")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        navController.navigate("quiz_results/$courseId")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Quiz Results")
                }

                if (pendingUsers.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Pending Enrolment Requests", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    pendingUsers.value.forEach { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(user.displayName, modifier = Modifier.weight(1f))
                                Row {
                                    TextButton(onClick = {
                                        viewModel.acceptEnrollment(courseId, user.id)
                                    }) {
                                        Text("Accept")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    TextButton(onClick = {
                                        viewModel.rejectEnrollment(courseId, user.id)
                                    }) {
                                        Text("Reject")
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // For Students
                val isEnrolled = course.enrolledUserIds.contains(currentUser?.id)
                val isPending = course.pendingUserIds.contains(currentUser?.id)

                Button(
                    onClick = {
                        if (isEnrolled) {
                            viewModel.unenrollFromCourse(courseId)
                        } else if (!isPending) {
                            viewModel.enrollInCourse(courseId)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isPending
                ) {
                    Text(
                        when {
                            isEnrolled -> "Unenroll from Course"
                            isPending -> "Enrolment Pending"
                            else -> "Request Enrolment"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Lessons", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            course.lessons.forEachIndexed { index, lesson ->
                val completed = lesson.completedByUserIds.contains(currentUser?.id)
                val unlocked = viewModel.canAccessLesson(course.lessons, index, currentUser?.id)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = unlocked) {
                            navController.navigate("${NavRoutes.LessonDetail.route}/$courseId/${lesson.id}")
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (unlocked)
                            MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = lesson.title +
                                when {
                                    completed -> " ✅"
                                    !unlocked -> " 🔒"
                                    else -> ""
                                },
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
