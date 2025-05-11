package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCourseScreen(navController: NavController, viewModel: MainViewModel) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val subjects = listOf("Computer Science", "Mathematics", "History", "Biology", "Art")
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create New Course",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "create_course_title"
                    contentDescription = "Create new course title"
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Course Title") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "title_field"
                                contentDescription = "Course title input"
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Course Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp)
                            .semantics {
                                testTag = "description_field"
                                contentDescription = "Course description input"
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            readOnly = true,
                            value = selectedSubject,
                            onValueChange = {},
                            label = { Text("Subject") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .semantics {
                                    testTag = "subject_dropdown"
                                    contentDescription = "Subject selection"
                                }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        selectedSubject = subject
                                        expanded = false
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Select $subject subject"
                                    }
                                )
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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a course title"
                        return@Button
                    }
                    if (description.isBlank()) {
                        errorMessage = "Please enter a course description"
                        return@Button
                    }
                    if (selectedSubject.isBlank()) {
                        errorMessage = "Please select a subject"
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        viewModel.createCourse(title, description, selectedSubject)
                        viewModel.refreshAllCourses()
                        isLoading = false
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "create_course_button"
                        contentDescription = "Create course"
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
                    Text("Create Course")
                }
            }
        }
    }
}