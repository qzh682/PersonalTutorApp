package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.flow.filter


@Composable
fun StudentDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser = viewModel.currentUser.collectAsState().value
    val allCourses by viewModel.allCourses.collectAsState()

    var searchQuery by remember { mutableStateOf("") }

    val filteredCourses = allCourses.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
                it.subject.contains(searchQuery, ignoreCase = true)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize()
        ) {
            Text(
                text = "Welcome, ${currentUser?.displayName ?: "Student"}",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search by name or subject") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Available Courses", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(12.dp))

            if (filteredCourses.isEmpty()) {
                Text("No courses found.", style = MaterialTheme.typography.bodyMedium)
            } else {
                filteredCourses.forEach { course ->
                    CourseCardWithStatus(
                        course = course,
                        currentUserId = currentUser?.id,
                        onClick = {
                            navController.navigate(NavRoutes.CourseDetail.createRoute(course.id))
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = course.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = course.subject, style = MaterialTheme.typography.bodySmall)

            currentUserId?.let {
                Spacer(modifier = Modifier.height(8.dp))
                when {
                    course.enrolledUserIds.contains(it) -> {
                        Text("✅ Enrolled", color = MaterialTheme.colorScheme.primary)
                    }
                    course.pendingUserIds.contains(it) -> {
                        Text("⌛ Enrolment Pending", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
