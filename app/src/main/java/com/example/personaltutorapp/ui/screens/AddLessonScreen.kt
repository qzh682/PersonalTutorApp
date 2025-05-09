package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.LessonPage
import com.example.personaltutorapp.model.PageType
import com.example.personaltutorapp.viewmodel.MainViewModel
import java.util.UUID

@Composable
fun AddLessonScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var pageType by remember { mutableStateOf(PageType.TEXT) }
    var pageContent by remember { mutableStateOf("") }
    var pages by remember { mutableStateOf(mutableListOf<LessonPage>()) }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Add Lesson", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Lesson Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Page Type:")
            Spacer(modifier = Modifier.width(8.dp))
            DropdownMenuBox(
                selectedType = pageType,
                onSelect = { pageType = it }
            )
        }

        OutlinedTextField(
            value = pageContent,
            onValueChange = { pageContent = it },
            label = { Text("Page Content (Text or Image URL)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            pages.add(
                LessonPage(
                    id = UUID.randomUUID().toString(),
                    type = pageType,
                    content = pageContent
                )
            )
            pageContent = ""
        }) {
            Text("Add Page")
        }

        Button(
            onClick = {
                viewModel.addLessonToCourse(courseId, title, pages)
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Lesson")
        }
    }
}

@Composable
fun DropdownMenuBox(selectedType: PageType, onSelect: (PageType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(onClick = { expanded = true }) {
            Text(selectedType.name)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("TEXT") }, onClick = {
                onSelect(PageType.TEXT)
                expanded = false
            })
            DropdownMenuItem(text = { Text("IMAGE") }, onClick = {
                onSelect(PageType.IMAGE)
                expanded = false
            })
        }
    }
}
