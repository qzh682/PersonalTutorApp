package com.example.personaltutorapp.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.personaltutorapp.model.PageType
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun LessonDetailScreen(
    courseId: String,
    lessonId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allCourses by viewModel.allCourses.collectAsState()

    // 🔄 自动随 allCourses 更新的 lesson 引用
    val lesson by remember(allCourses, courseId, lessonId) {
        derivedStateOf {
            allCourses.find { it.id == courseId }
                ?.lessons?.find { it.id == lessonId }
        }
    }

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
                            contentDescription = "Lesson Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(vertical = 8.dp)
                        )
                    }
                    PageType.PDF -> {
                        Text(
                            text = "View PDF",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(page.content))
                                    context.startActivity(intent)
                                }
                        )
                    }
                    PageType.AUDIO -> {
                        Text(
                            text = "Play Audio",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(page.content), "audio/*")
                                    }
                                    context.startActivity(intent)
                                }
                        )
                    }
                    PageType.VIDEO -> {
                        Text(
                            text = "Watch Video",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .clickable {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        setDataAndType(Uri.parse(page.content), "video/*")
                                    }
                                    context.startActivity(intent)
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.markLessonCompleted(courseId, lessonId)
                        viewModel.refreshAllCourses()
                        navController.popBackStack()
                    }
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
