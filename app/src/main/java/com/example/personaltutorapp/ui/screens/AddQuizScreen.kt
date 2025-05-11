package com.example.personaltutorapp.ui.screens

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
import com.example.personaltutorapp.model.QuizQuestion
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AddQuizScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    val questions = remember { mutableStateListOf<QuizQuestion>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Add Quiz",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "add_quiz_title"
                    contentDescription = "Add quiz title"
                }
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "question_field"
                                contentDescription = "Question input"
                            }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = optionA,
                        onValueChange = { optionA = it },
                        label = { Text("Option A") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "option_a_field"
                                contentDescription = "Option A input"
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = optionB,
                        onValueChange = { optionB = it },
                        label = { Text("Option B") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "option_b_field"
                                contentDescription = "Option B input"
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = optionC,
                        onValueChange = { optionC = it },
                        label = { Text("Option C") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "option_c_field"
                                contentDescription = "Option C input"
                            }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = optionD,
                        onValueChange = { optionD = it },
                        label = { Text("Option D") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "option_d_field"
                                contentDescription = "Option D input"
                            }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Correct Answer",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics {
                            testTag = "correct_answer_label"
                            contentDescription = "Correct answer label"
                        }
                    )
                    Row {
                        listOf("A", "B", "C", "D").forEach { option ->
                            Row(
                                modifier = Modifier
                                    .padding(end = 16.dp)
                                    .semantics {
                                        testTag = "correct_answer_$option"
                                        contentDescription = "Select $option as correct answer"
                                    }
                            ) {
                                RadioButton(
                                    selected = correctAnswer == option,
                                    onClick = { correctAnswer = option },
                                    modifier = Modifier.semantics { testTag = "radio_$option" }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = option, style = MaterialTheme.typography.bodyMedium)
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

            Button(
                onClick = {
                    if (questionText.isBlank() || optionA.isBlank() || optionB.isBlank() ||
                        optionC.isBlank() || optionD.isBlank()) {
                        errorMessage = "Please fill all fields"
                        return@Button
                    }

                    val correctAnswerIndex = when (correctAnswer) {
                        "A" -> 0
                        "B" -> 1
                        "C" -> 2
                        "D" -> 3
                        else -> 0
                    }

                    questions.add(
                        QuizQuestion(
                            id = UUID.randomUUID().toString(),
                            question = questionText,
                            options = listOf(optionA, optionB, optionC, optionD),
                            correctAnswerIndex = correctAnswerIndex
                        )
                    )

                    questionText = ""
                    optionA = ""
                    optionB = ""
                    optionC = ""
                    optionD = ""
                    correctAnswer = "A"
                    errorMessage = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "add_question_button"
                        contentDescription = "Add question"
                    },
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Add Question")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (questions.isEmpty()) {
                        errorMessage = "Please add at least one question"
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        viewModel.addQuizToCourse(courseId, questions)
                        isLoading = false
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "save_quiz_button"
                        contentDescription = "Save quiz"
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
                    Text("Save Quiz")
                }
            }
        }
    }
}