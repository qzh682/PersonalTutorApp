package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.model.User
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun TutorDashboard(
    navController: NavController,
    viewModel: MainViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val courses = viewModel.getCoursesForCurrentUser()
    val pendingUsersMap = remember { mutableStateMapOf<String, List<User>>() }

    LaunchedEffect(courses) {
        courses.forEach { course ->
            val users = course.pendingUserIds.mapNotNull { userId ->
                viewModel.getUserByIdSuspend(userId)
            }
            pendingUsersMap[course.id] = users
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome, ${currentUser?.displayName}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate(NavRoutes.CreateCourse.route) }) {
            Text("Create New Course")
        }

        Spacer(modifier = Modifier.height(24.dp))

        courses.forEach { course ->
            CourseCard(course = course) {
                navController.navigate("${NavRoutes.CourseDetail.route}/${course.id}")
            }

            val totalLessons = course.lessons.size
            val enrolledCount = course.enrolledUserIds.size
            val avgProgress = if (totalLessons > 0 && enrolledCount > 0) {
                val totalCompleted = course.enrolledUserIds.sumOf { userId ->
                    course.lessons.count { lesson -> userId in lesson.completedByUserIds }
                }
                (totalCompleted.toFloat() / (totalLessons * enrolledCount) * 100).toInt()
            } else 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Enrolled Students: $enrolledCount", style = MaterialTheme.typography.bodyMedium)
                    Text("Average Progress: $avgProgress%", style = MaterialTheme.typography.bodyMedium)
                    ProgressBar(percentage = avgProgress)
                }
            }

            val pendingUsers = pendingUsersMap[course.id] ?: emptyList()
            if (pendingUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Pending Requests:", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(4.dp))

                pendingUsers.forEach { pendingUser ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pendingUser.displayName, modifier = Modifier.weight(1f))
                        Button(onClick = {
                            viewModel.acceptEnrollment(course.id, pendingUser.id)
                        }) {
                            Text("Accept")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = {
                            viewModel.rejectEnrollment(course.id, pendingUser.id)
                        }) {
                            Text("Reject")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(course.title, style = MaterialTheme.typography.titleMedium)
            Text(course.subject, style = MaterialTheme.typography.bodySmall)
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
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentage / 100f)
                .height(12.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}
