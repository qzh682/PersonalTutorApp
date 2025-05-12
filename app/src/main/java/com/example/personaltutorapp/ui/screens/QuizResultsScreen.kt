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
import com.example.personaltutorapp.viewmodel.MainViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@Composable
fun QuizResultsScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    val course = viewModel.getCourseById(courseId)
    var quizResults by remember { mutableStateOf<List<MainViewModel.QuizResult>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(courseId) {
        loadQuizResults(viewModel, courseId) { results, error ->
            quizResults = results
            errorMessage = error
            isLoading = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading) {
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
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = errorMessage ?: "An error occurred",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics {
                                testTag = "error_message"
                                contentDescription = "Error message"
                            }
                        )
                        Button(
                            onClick = {
                                isLoading = true
                                errorMessage = null
                                loadQuizResults(viewModel, courseId) { results, error ->
                                    quizResults = results
                                    errorMessage = error
                                    isLoading = false
                                }
                            },
                            modifier = Modifier.semantics {
                                testTag = "retry_button"
                                contentDescription = "Retry loading quiz results"
                            }
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                Text(
                    text = "Quiz Results for ${course?.title ?: "Course"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        testTag = "quiz_results_title"
                        contentDescription = "Quiz results for course ${course?.title ?: "Course"}"
                    }
                )

                if (course?.quiz == null) {
                    Text(
                        text = "No quiz available for this course",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics {
                            testTag = "no_quiz_text"
                            contentDescription = "No quiz available"
                        }
                    )
                } else {
                    Text(
                        text = if (course.quiz.isPublished) "Quiz Status: Published" else "Quiz Status: Not Published",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.semantics {
                            testTag = "quiz_status"
                            contentDescription = if (course.quiz.isPublished) "Quiz published" else "Quiz not published"
                        }
                    )

                    if (quizResults.isEmpty()) {
                        Text(
                            text = "No quiz results available",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "no_results_text"
                                contentDescription = "No quiz results available"
                            }
                        )
                    } else {
                        quizResults.forEach { result ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .semantics {
                                        testTag = "result_card_${result.studentName}"
                                        contentDescription = "Quiz result for student ${result.studentName}"
                                    },
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Student: ${result.studentName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.semantics {
                                            testTag = "student_name_${result.studentName}"
                                            contentDescription = "Student: ${result.studentName}"
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Score: ${result.score} / ${result.total}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.semantics {
                                            testTag = "score_${result.studentName}"
                                            contentDescription = "Score: ${result.score} out of ${result.total}"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics {
                            testTag = "back_button"
                            contentDescription = "Back to dashboard"
                        },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Back to Dashboard")
                }
            }
        }
    }
}

private fun loadQuizResults(
    viewModel: MainViewModel,
    courseId: String,
    onResult: (List<MainViewModel.QuizResult>, String?) -> Unit
) {
    viewModel.viewModelScope.launch {
        try {
            val results = viewModel.getQuizResults(courseId)
            println("Loaded quiz results for course $courseId: ${results.size} results")
            onResult(results, null)
        } catch (e: Exception) {
            println("Failed to load quiz results: ${e.message}")
            onResult(emptyList(), "Failed to load quiz results: ${e.message}")
        }
    }
}