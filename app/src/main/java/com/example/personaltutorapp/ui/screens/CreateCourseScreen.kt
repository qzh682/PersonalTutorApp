package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun CreateCourseScreen(navController: NavController, viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val subjects = listOf("Computer Science", "Mathematics", "History", "Biology", "Art")

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text("Create New Course", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Course Title") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Course Description") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text("Select Subject", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(4.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable { isDropdownExpanded = true }
            .padding(8.dp)) {
            Text(text = if (selectedSubject.isNotEmpty()) selectedSubject else "Choose subject...")
        }

        DropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = { Text(subject) },
                    onClick = {
                        selectedSubject = subject
                        isDropdownExpanded = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && description.isNotBlank() && selectedSubject.isNotBlank()) {
                    viewModel.createCourse(title, description, selectedSubject)
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Create Course")
        }
    }
}
