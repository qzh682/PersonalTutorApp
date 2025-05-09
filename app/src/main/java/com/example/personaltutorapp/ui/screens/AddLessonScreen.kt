package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.LessonPage
import com.example.personaltutorapp.model.PageType
import com.example.personaltutorapp.viewmodel.MainViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLessonScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var pageType by remember { mutableStateOf(PageType.TEXT) }
    var pageContent by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf(mutableListOf<LessonPage>()) }
    var expanded by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Add Lesson", style = MaterialTheme.typography.headlineMedium)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Lesson Title") },
                modifier = Modifier.fillMaxWidth()
            )

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
                            }
                        )
                    }
                }
            }

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
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (pageContent.isNotBlank()) {
                        pages.add(
                            LessonPage(
                                id = UUID.randomUUID().toString(),
                                type = pageType,
                                content = pageContent
                            )
                        )
                        pageContent = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Page to Lesson")
            }

            if (pages.isNotEmpty()) {
                Text("Pages Added: ${pages.size}", style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && pages.isNotEmpty()) {
                        viewModel.addLessonToCourse(courseId, title, pages)
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Lesson")
            }
        }
    }
}
