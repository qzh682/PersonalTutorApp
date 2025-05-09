package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.personaltutorapp.model.PageType
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun LessonDetailScreen(
    courseId: String,
    lessonId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val lesson = viewModel.getCourseById(courseId)?.lessons?.find { it.id == lessonId }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = lesson?.title ?: "", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        lesson?.pages?.forEach { page ->
            when (page.type) {
                PageType.TEXT -> {
                    Text(
                        text = page.content,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                PageType.IMAGE -> {
                    Image(
                        painter = rememberAsyncImagePainter(page.content),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            viewModel.markLessonCompleted(courseId, lessonId)
            navController.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Mark as Completed")
        }
    }
}
