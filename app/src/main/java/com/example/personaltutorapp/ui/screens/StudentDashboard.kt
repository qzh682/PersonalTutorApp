package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    val courses = viewModel.allCourses

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome, ${currentUser?.displayName ?: "Student"}", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Available Courses:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        courses.forEach { course ->
            CourseCard(course = course) {
                navController.navigate(NavRoutes.CourseDetail.createRoute(course.id))
            }
        }
    }
}


