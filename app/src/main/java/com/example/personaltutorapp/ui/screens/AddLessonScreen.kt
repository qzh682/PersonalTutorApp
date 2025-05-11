package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.LessonPage
import com.example.personaltutorapp.model.PageType
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.*
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLessonScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var pageType by remember { mutableStateOf(PageType.TEXT) }
    var pageContent by remember { mutableStateOf("") }
    val pages = remember { mutableStateListOf<LessonPage>() }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // URL 验证函数
    fun isValidUrl(url: String, type: PageType): Boolean {
        // 基本 URL 格式验证（以 http:// 或 https:// 开头）
        val basicUrlPattern = Pattern.compile(
            "^(https?://.*)$",
            Pattern.CASE_INSENSITIVE
        )
        // 文件扩展名验证（用于 PDF、音频、视频）
        val strictUrlPattern = Pattern.compile(
            "^(https?://.*\\.(?:pdf|mp3|mp4))$",
            Pattern.CASE_INSENSITIVE
        )
        return when (type) {
            PageType.IMAGE -> basicUrlPattern.matcher(url).matches() // 放宽图片 URL 验证
            PageType.PDF -> strictUrlPattern.matcher(url).matches() && url.endsWith("pdf", true)
            PageType.AUDIO -> strictUrlPattern.matcher(url).matches() && url.endsWith("mp3", true)
            PageType.VIDEO -> strictUrlPattern.matcher(url).matches() && url.endsWith("mp4", true)
            else -> true // TEXT 类型不需要 URL 验证
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add Lesson",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "add_lesson_title"
                    contentDescription = "Add lesson title"
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Lesson Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "title_field"
                                contentDescription = "Lesson title input"
                            },
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = pageType.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Page Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .clickable { expanded = true }
                                .semantics {
                                    testTag = "page_type_dropdown"
                                    contentDescription = "Page type selection"
                                },
                            enabled = !isLoading
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            PageType.values().forEach {
                                DropdownMenuItem(
                                    text = { Text(it.name) },
                                    onClick = {
                                        pageType = it
                                        expanded = false
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Select ${it.name} page type"
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = pageContent,
                        onValueChange = { pageContent = it },
                        label = {
                            Text(
                                when (pageType) {
                                    PageType.TEXT -> "Enter Text"
                                    PageType.IMAGE -> "Enter Image URL"
                                    PageType.PDF -> "Enter PDF URL"
                                    PageType.AUDIO -> "Enter MP3 URL"
                                    PageType.VIDEO -> "Enter MP4 URL"
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "content_field"
                                contentDescription = "Page content input"
                            },
                        enabled = !isLoading
                    )
                }
            }

            // 显示已添加的页面预览
            if (pages.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pages Added: ${pages.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "pages_count"
                                contentDescription = "Pages added: ${pages.size}"
                            }
                        )
                        pages.forEachIndexed { index, page ->
                            Text(
                                text = "Page ${index + 1}: ${page.type.name} - ${page.content.take(30)}${if (page.content.length > 30) "..." else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.semantics {
                                    testTag = "page_preview_$index"
                                    contentDescription = "Page ${index + 1} preview"
                                }
                            )
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

            Button(
                onClick = {
                    if (pageContent.isBlank()) {
                        errorMessage = "Please enter page content"
                        return@Button
                    }
                    if (!isValidUrl(pageContent, pageType)) {
                        errorMessage = when (pageType) {
                            PageType.IMAGE -> "Invalid URL format for ${pageType.name}. Please use a valid URL starting with http:// or https://"
                            PageType.PDF -> "Invalid URL format for ${pageType.name}. Please use a valid URL ending with pdf"
                            PageType.AUDIO -> "Invalid URL format for ${pageType.name}. Please use a valid URL ending with mp3"
                            PageType.VIDEO -> "Invalid URL format for ${pageType.name}. Please use a valid URL ending with mp4"
                            else -> "Invalid URL format"
                        }
                        return@Button
                    }
                    val newPage = LessonPage(
                        id = UUID.randomUUID().toString(),
                        type = pageType,
                        content = pageContent
                    )
                    pages.add(newPage)
                    println("Added page: $newPage") // 调试日志
                    pageContent = ""
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "add_page_button"
                        contentDescription = "Add page to lesson"
                    },
                shape = MaterialTheme.shapes.medium,
                enabled = !isLoading
            ) {
                Text("Add Page to Lesson")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a lesson title"
                        return@Button
                    }
                    if (pages.isEmpty()) {
                        errorMessage = "Please add at least one page"
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            viewModel.addLessonToCourse(courseId, title, pages)
                            viewModel.refreshAllCourses() // 确保数据刷新
                            println("Lesson saved: $title with ${pages.size} pages") // 调试日志
                            isLoading = false
                            navController.popBackStack()
                        } catch (e: Exception) {
                            errorMessage = "Failed to save lesson: ${e.message}"
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "save_lesson_button"
                        contentDescription = "Save lesson"
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
                    Text("Save Lesson")
                }
            }
        }
    }
}