package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
        ) {
            Text(
                text = lesson?.title ?: "Lesson",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(24.dp))

            lesson?.pages?.forEach { page ->
                when (page.type) {
                    PageType.TEXT -> {
                        Text(
                            text = page.content,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    PageType.IMAGE -> {
                        Image(
                            painter = rememberAsyncImagePainter(page.content),
                            contentDescription = "Lesson image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.markLessonCompleted(courseId, lessonId)
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Mark as Completed")
            }
        }
    }
}
