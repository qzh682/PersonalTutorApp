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
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun StudentDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(allCourses) {
        isLoading = false
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
            Text(
                text = "Welcome, ${currentUser?.displayName ?: "Student"}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "welcome_text"
                    contentDescription = "Welcome message"
                }
            )

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
    onClick: () -> Unit
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