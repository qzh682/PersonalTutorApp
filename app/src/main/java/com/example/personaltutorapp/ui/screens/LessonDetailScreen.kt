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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.personaltutorapp.model.PageType
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import com.example.personaltutorapp.R
import androidx.compose.ui.res.painterResource

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
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 🔄 自动随 allCourses 更新的 lesson 引用
    val lesson by remember(allCourses, courseId, lessonId) {
        derivedStateOf {
            allCourses.find { it.id == courseId }?.lessons?.find { it.id == lessonId }
        }
    }

    // 强制刷新数据
    LaunchedEffect(Unit) {
        viewModel.refreshAllCourses()
    }

    // 调试 lesson 数据
    LaunchedEffect(lesson) {
        println("Lesson pages: ${lesson?.pages}")
        isLoading = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
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
            } else if (lesson == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lesson not found",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics {
                            testTag = "lesson_not_found"
                            contentDescription = "Lesson not found"
                        }
                    )
                }
            } else {
                lesson?.let { lesson ->
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.semantics {
                            testTag = "lesson_title"
                            contentDescription = "Lesson title: ${lesson.title}"
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (lesson.pages.isEmpty()) {
                        Text(
                            text = "No pages available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "no_pages_text"
                                contentDescription = "No pages available"
                            }
                        )
                    } else {
                        lesson.pages.forEach { page ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .semantics {
                                        testTag = "page_${page.id}"
                                        contentDescription = "Page: ${page.type.name}"
                                    },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    when (page.type) {
                                        PageType.TEXT -> {
                                            Text(
                                                text = page.content,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.semantics {
                                                    testTag = "text_page_${page.id}"
                                                    contentDescription = "Text content"
                                                }
                                            )
                                        }
                                        PageType.IMAGE -> {
                                            var retryTrigger by remember { mutableStateOf(0) } // 用于触发重试
                                            // 使用 retryTrigger 修改 model，触发重新加载
                                            val imageModel by remember(retryTrigger) {
                                                mutableStateOf(page.content + "?retry=$retryTrigger")
                                            }
                                            val painter = rememberAsyncImagePainter(
                                                model = imageModel,
                                                placeholder = painterResource(id = try { R.drawable.placeholder } catch (e: Exception) { android.R.drawable.ic_menu_gallery }),
                                                error = painterResource(id = try { R.drawable.error_image } catch (e: Exception) { android.R.drawable.ic_menu_report_image }),
                                                onLoading = { println("Loading image: ${page.content}, retryTrigger: $retryTrigger") },
                                                onSuccess = { println("Image loaded successfully: ${page.content}") },
                                                onError = { error ->
                                                    val errorMsg = "Failed to load image: ${page.content}. Reason: ${error.result.throwable.message}"
                                                    println(errorMsg)
                                                    errorMessage = errorMsg
                                                }
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp)
                                            ) {
                                                Image(
                                                    painter = painter,
                                                    contentDescription = "Lesson image",
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .semantics {
                                                            testTag = "image_page_${page.id}"
                                                        }
                                                )
                                                when (painter.state) {
                                                    is AsyncImagePainter.State.Loading -> {
                                                        CircularProgressIndicator(
                                                            modifier = Modifier
                                                                .align(Alignment.Center)
                                                                .size(32.dp)
                                                        )
                                                    }
                                                    is AsyncImagePainter.State.Error -> {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxSize()
                                                                .align(Alignment.Center),
                                                            horizontalAlignment = Alignment.CenterHorizontally
                                                        ) {
                                                            Image(
                                                                painter = painterResource(id = try { R.drawable.error_image } catch (e: Exception) { android.R.drawable.ic_menu_report_image }),
                                                                contentDescription = "Error image",
                                                                modifier = Modifier.size(64.dp)
                                                            )
                                                            Spacer(modifier = Modifier.height(8.dp))
                                                            Button(
                                                                onClick = { retryTrigger++ }, // 触发重试
                                                                modifier = Modifier.semantics {
                                                                    testTag = "retry_button_${page.id}"
                                                                    contentDescription = "Retry loading image"
                                                                }
                                                            ) {
                                                                Text("Retry")
                                                            }
                                                        }
                                                    }
                                                    else -> Unit
                                                }
                                            }
                                        }
                                        PageType.PDF -> {
                                            Text(
                                                text = "View PDF",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .clickable {
                                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(page.content))
                                                        try {
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            errorMessage = "Failed to open PDF: ${e.message}"
                                                        }
                                                    }
                                                    .semantics {
                                                        testTag = "pdf_page_${page.id}"
                                                        contentDescription = "Open PDF file"
                                                    }
                                            )
                                        }
                                        PageType.AUDIO -> {
                                            Text(
                                                text = "Play Audio",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .clickable {
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            setDataAndType(Uri.parse(page.content), "audio/*")
                                                        }
                                                        try {
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            errorMessage = "Failed to play audio: ${e.message}"
                                                        }
                                                    }
                                                    .semantics {
                                                        testTag = "audio_page_${page.id}"
                                                        contentDescription = "Play audio file"
                                                    }
                                            )
                                        }
                                        PageType.VIDEO -> {
                                            Text(
                                                text = "Watch Video",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .clickable {
                                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                                            setDataAndType(Uri.parse(page.content), "video/*")
                                                        }
                                                        try {
                                                            context.startActivity(intent)
                                                        } catch (e: Exception) {
                                                            errorMessage = "Failed to play video: ${e.message}"
                                                        }
                                                    }
                                                    .semantics {
                                                        testTag = "video_page_${page.id}"
                                                        contentDescription = "Play video file"
                                                    }
                                            )
                                        }
                                        else -> {
                                            Text(
                                                text = "Unsupported page type: ${page.type}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.semantics {
                                                    testTag = "unsupported_page_${page.id}"
                                                    contentDescription = "Unsupported page type"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    errorMessage?.let {
                        Snackbar(
                            modifier = Modifier.padding(top = 8.dp),
                            action = {
                                TextButton(onClick = { errorMessage = null }) { Text("Dismiss") }
                            }
                        ) { Text(it) }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                try {
                                    viewModel.markLessonCompleted(courseId, lessonId)
                                    viewModel.refreshAllCourses()
                                    println("Marked lesson as completed: $lessonId")
                                } catch (e: Exception) {
                                    errorMessage = "Failed to mark lesson as completed: ${e.message}"
                                } finally {
                                    isLoading = false
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .semantics {
                                testTag = "complete_button"
                                contentDescription = "Mark as completed"
                            },
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Mark as Completed")
                        }
                    }
                }
            }
        }
    }
}