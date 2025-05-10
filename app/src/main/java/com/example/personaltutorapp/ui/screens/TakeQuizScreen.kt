package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun TakeQuizScreen(
    courseId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val course = viewModel.getCourseById(courseId)
    val quiz = course?.quiz
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateListOf<Int?>() }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }

    LaunchedEffect(quiz) {
        selectedAnswers.clear()
        quiz?.questions?.forEach { _ -> selectedAnswers.add(null) }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (quiz == null) {
                Text("No quiz available.", style = MaterialTheme.typography.titleMedium)
                return@Column
            }

            if (showResult) {
                Text("You scored: $score / ${quiz.questions.size}", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Course")
                }
                return@Column
            }

            val question = quiz.questions.getOrNull(currentIndex)
            if (question != null) {
                Text("Question ${currentIndex + 1} of ${quiz.questions.size}", style = MaterialTheme.typography.titleMedium)
                Text(question.question, style = MaterialTheme.typography.bodyLarge)

                question.options.forEachIndexed { index, option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnswers[currentIndex] = index }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = selectedAnswers[currentIndex] == index,
                            onClick = { selectedAnswers[currentIndex] = index }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(option)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (currentIndex > 0) {
                        Button(onClick = { currentIndex-- }) {
                            Text("Previous")
                        }
                    }
                    if (currentIndex < quiz.questions.size - 1) {
                        Button(onClick = { currentIndex++ }) {
                            Text("Next")
                        }
                    } else {
                        Button(
                            onClick = {
                                val correctCount = quiz.questions.mapIndexed { i, q ->
                                    if (selectedAnswers[i] == q.correctAnswerIndex) 1 else 0
                                }.sum()
                                score = correctCount

                                scope.launch {
                                    if (currentUser != null) {
                                        viewModel.submitQuizResult(
                                            courseId = courseId,
                                            userId = currentUser!!.id,
                                            score = correctCount
                                        )
                                    }
                                }

                                showResult = true
                            }
                        ) {
                            Text("Submit Quiz")
                        }
                    }
                }
            }
        }
    }
}
