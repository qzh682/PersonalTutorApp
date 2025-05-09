package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.viewmodel.MainViewModel
import com.example.personaltutorapp.navigation.NavRoutes

@Composable
fun CourseDetailScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    val course = viewModel.getCourseById(courseId)
    val currentUserId = viewModel.currentUser.value?.id

    Column(modifier = Modifier.padding(16.dp)) {
        course?.let {
            Text(text = it.title, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Subject: ${it.subject}", style = MaterialTheme.typography.bodyLarge)
            Text(text = it.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.currentUser.value?.id == it.tutor.id) {
                Button(
                    onClick = {
                        navController.navigate("${NavRoutes.AddLesson.route}/$courseId")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Lesson")
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Lessons:", style = MaterialTheme.typography.titleMedium)
            it.lessons.forEach { lesson ->
                val completed = lesson.completedByUserIds.contains(currentUserId)
                Text(
                    text = lesson.title + if (completed) " ✅" else "",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("${NavRoutes.LessonDetail.route}/$courseId/${lesson.id}")
                        }
                        .padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
