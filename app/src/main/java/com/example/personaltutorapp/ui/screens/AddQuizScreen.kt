package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.model.QuizQuestion
import com.example.personaltutorapp.viewmodel.MainViewModel
import java.util.UUID

@Composable
fun AddQuizScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    var questions by remember { mutableStateOf(mutableListOf<QuizQuestion>()) }

    Column(modifier = Modifier.padding(24.dp)) {
        Text("Add Quiz", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = questionText,
            onValueChange = { questionText = it },
            label = { Text("Question") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(value = optionA, onValueChange = { optionA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = optionB, onValueChange = { optionB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = optionC, onValueChange = { optionC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = optionD, onValueChange = { optionD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        Text("Correct Answer:")
        Row {
            listOf("A", "B", "C", "D").forEach { option ->
                Row(modifier = Modifier.padding(end = 16.dp)) {
                    RadioButton(
                        selected = correctAnswer == option,
                        onClick = { correctAnswer = option }
                    )
                    Text(option)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (questionText.isNotBlank() && optionA.isNotBlank() && optionB.isNotBlank()
                && optionC.isNotBlank() && optionD.isNotBlank()) {

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
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Add Question")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (questions.isNotEmpty()) {
                viewModel.addQuizToCourse(courseId, questions)
                navController.popBackStack()
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Quiz")
        }
    }
}
