package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.Course
import com.example.personaltutorapp.viewmodel.MainViewModel
import com.example.personaltutorapp.navigation.NavRoutes

@Composable
fun TutorDashboard(navController: NavController, viewModel: MainViewModel) {
    val user = viewModel.currentUser.collectAsState().value
    val courses = viewModel.getCoursesForCurrentUser()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Welcome, ${user?.displayName}", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate(NavRoutes.CreateCourse.route) }) {
            Text("Create New Course")
        }

        Spacer(modifier = Modifier.height(24.dp))

        courses.forEach { course ->
            CourseCard(course = course) {
                navController.navigate(NavRoutes.CourseDetail.route + "/${course.id}")
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Text(text = course.title, style = MaterialTheme.typography.titleMedium)
        Text(text = course.subject, style = MaterialTheme.typography.bodySmall)
    }
}
