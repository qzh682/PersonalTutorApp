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
import com.example.personaltutorapp.navigation.NavRoutes
import kotlinx.coroutines.launch

@Composable
fun QuizScreen(
    courseId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val course = viewModel.getCourseById(courseId)
    val quiz = course?.quiz
    val currentUser = viewModel.currentUser.collectAsState().value
    val scope = rememberCoroutineScope()

    if (quiz == null || currentUser == null) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("No quiz available for this course.", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    val submission = quiz.submissions.find { it.userId == currentUser.id }
    var showQuiz by remember { mutableStateOf(submission == null) }
    var currentIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateListOf<Int?>() }
    var score by remember { mutableStateOf(0) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(showQuiz) {
        if (showQuiz) {
            selectedAnswers.clear()
            quiz.questions.forEach { _ -> selectedAnswers.add(null) }
            currentIndex = 0
            completed = false
            score = 0
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            if (!showQuiz && submission != null) {
                Text("You’ve already completed this quiz.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Score: ${submission.score} / ${quiz.questions.size}", style = MaterialTheme.typography.bodyLarge)

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showQuiz = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Retake Quiz")
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to Course")
                }
            } else if (!completed) {
                val question = quiz.questions.getOrNull(currentIndex)
                if (question != null) {
                    Text("Question ${currentIndex + 1} / ${quiz.questions.size}", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(question.question, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(16.dp))

                    question.options.forEachIndexed { index, option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedAnswers[currentIndex] = index }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = selectedAnswers[currentIndex] == index,
                                onClick = { selectedAnswers[currentIndex] = index }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(option)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        if (currentIndex > 0) {
                            Button(onClick = { currentIndex-- }) {
                                Text("Previous")
                            }
                        }
                        Button(
                            onClick = {
                                if (currentIndex < quiz.questions.size - 1) {
                                    currentIndex++
                                } else {
                                    score = quiz.questions.mapIndexed { i, q ->
                                        if (selectedAnswers[i] == q.correctAnswerIndex) 1 else 0
                                    }.sum()

                                    scope.launch {
                                        viewModel.submitQuizResult(courseId, currentUser.id, score)
                                    }

                                    completed = true
                                }
                            },
                            enabled = selectedAnswers[currentIndex] != null
                        ) {
                            Text(if (currentIndex < quiz.questions.size - 1) "Next" else "Submit")
                        }
                    }
                }
            } else {
                Text("Quiz Completed!", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))
                Text("You scored $score out of ${quiz.questions.size}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to Course")
                }
            }
        }
    }
}
