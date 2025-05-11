package com.example.personaltutorapp.ui.screens

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import coil.request.ImageRequest
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

    // 验证参数
    LaunchedEffect(courseId, lessonId) {
        if (courseId.isBlank()) {
            println("Error: courseId is blank")
            errorMessage = "Invalid course ID"
            isLoading = false
        }
        if (lessonId.isBlank()) {
            println("Error: lessonId is blank")
            errorMessage = "Invalid lesson ID"
            isLoading = false
        }
    }

    // 检查网络状态
    var isNetworkAvailable by remember { mutableStateOf(false) }
    try {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        isNetworkAvailable = connectivityManager?.let {
            val network = it.activeNetwork ?: return@let false
            val capabilities = it.getNetworkCapabilities(network) ?: return@let false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
        println("Network available: $isNetworkAvailable")
    } catch (e: SecurityException) {
        println("Failed to check network state: ${e.message}")
        isNetworkAvailable = false
        errorMessage = "Cannot check network state: Missing ACCESS_NETWORK_STATE permission"
    } catch (e: Exception) {
        println("Unexpected error while checking network state: ${e.message}")
        isNetworkAvailable = false
        errorMessage = "Error checking network state: ${e.message}"
    }

    // 🔄 自动随 allCourses 更新的 lesson 引用
    val lesson by remember(allCourses, courseId, lessonId) {
        derivedStateOf {
            println("Finding lesson: courseId=$courseId, lessonId=$lessonId")
            val course = allCourses.find { it.id == courseId }
            if (course == null) {
                println("Course $courseId not found in allCourses: ${allCourses.map { it.id }}")
            }
            val foundLesson = course?.lessons?.find { it.id == lessonId }
            if (foundLesson == null) {
                println("Lesson $lessonId not found in course $courseId")
            }
            foundLesson
        }
    }

    // 强制刷新数据
    LaunchedEffect(Unit) {
        println("Refreshing all courses on LessonDetailScreen entry")
        try {
            viewModel.refreshAllCourses()
        } catch (e: Exception) {
            println("Failed to refresh courses: ${e.message}")
            errorMessage = "Failed to load lesson data: ${e.message}"
            isLoading = false
        }
    }

    // 调试 lesson 数据
    LaunchedEffect(lesson) {
        println("Lesson data updated: $lesson")
        isLoading = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = errorMessage ?: "An error occurred",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics {
                            testTag = "error_message"
                            contentDescription = "Error message"
                        }
                    )
                }
            } else if (isLoading) {
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
                        text = "Lesson not found. Course ID: $courseId, Lesson ID: $lessonId",
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
                                            if (!isNetworkAvailable) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(220.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Text(
                                                        text = "No internet connection",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        modifier = Modifier.semantics {
                                                            testTag = "no_internet_${page.id}"
                                                            contentDescription = "No internet connection"
                                                        }
                                                    )
                                                }
                                            } else {
                                                var retryTrigger by remember { mutableStateOf(0) }
                                                var isImageLoaded by remember { mutableStateOf(false) }
                                                val imageModel by remember(retryTrigger) {
                                                    mutableStateOf(page.content + "?retry=$retryTrigger")
                                                }
                                                val painter = rememberAsyncImagePainter(
                                                    model = ImageRequest.Builder(context)
                                                        .data(imageModel)
                                                        .size(256, 256)
                                                        .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                                        .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                                        .allowHardware(false)
                                                        .build(),
                                                    placeholder = painterResource(id = try { R.drawable.placeholder } catch (e: Exception) { android.R.drawable.ic_menu_gallery }),
                                                    error = painterResource(id = try { R.drawable.error_image } catch (e: Exception) { android.R.drawable.ic_menu_report_image }),
                                                    onLoading = { println("Loading image: ${page.content}, retryTrigger: $retryTrigger") },
                                                    onSuccess = {
                                                        println("Image loaded successfully: ${page.content}")
                                                        isImageLoaded = true
                                                    },
                                                    onError = { error ->
                                                        val errorMsg = if (error.result.throwable.message?.contains("BitmapFactory returned a null bitmap") == true) {
                                                            "Failed to load image: ${page.content}. The URL may not point to a valid image. Please use a direct image URL ending with .jpg, .png, etc. (e.g., https://cdn.pixabay.com/photo/2023/01/01/image.jpg)"
                                                        } else {
                                                            "Failed to load image: ${page.content}. Reason: ${error.result.throwable.message}"
                                                        }
                                                        println(errorMsg)
                                                        errorMessage = errorMsg
                                                        isImageLoaded = false
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
                                                                    onClick = { retryTrigger++ },
                                                                    modifier = Modifier.semantics {
                                                                        testTag = "retry_button_${page.id}"
                                                                        contentDescription = "Retry loading image"
                                                                    }
                                                                ) {
                                                                    Text("Retry")
                                                                }
                                                            }
                                                        }
                                                        is AsyncImagePainter.State.Success -> {
                                                            LaunchedEffect(isImageLoaded) {
                                                                if (isImageLoaded) {
                                                                    println("Image displayed: ${page.content}")
                                                                }
                                                            }
                                                        }
                                                        else -> Unit
                                                    }
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
                                                            println("Error opening PDF: ${e.message}")
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
                                                            println("Error playing audio: ${e.message}")
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
                                                            println("Error playing video: ${e.message}")
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
                                    println("Error marking lesson as completed: ${e.message}")
                                } finally {
                                    try {
                                        navController.popBackStack()
                                        println("Navigation: Popped back stack")
                                    } catch (e: Exception) {
                                        errorMessage = "Failed to navigate back: ${e.message}"
                                        println("Error navigating back: ${e.message}")
                                    }
                                    isLoading = false
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