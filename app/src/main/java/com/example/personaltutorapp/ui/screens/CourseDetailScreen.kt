package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun CourseDetailScreen(
    navController: NavController,
    viewModel: MainViewModel,
    courseId: String
) {
    val course = viewModel.getCourseById(courseId)
    val currentUser by viewModel.currentUser.collectAsState()

    if (course == null) {
        Text("Course not found")
        return
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = course.title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = course.description)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Subject: ${course.subject}")

        if (course.tutor.id == currentUser?.id) {
            Button(onClick = {
                navController.navigate(NavRoutes.AddLesson.createRoute(courseId))
            }, modifier = Modifier.padding(top = 16.dp)) {
                Text("Add Lesson")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Lessons:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        course.lessons.forEach { lesson ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                onClick = {
                    navController.navigate(NavRoutes.LessonDetail.createRoute(courseId, lesson.id))
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = lesson.title, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
