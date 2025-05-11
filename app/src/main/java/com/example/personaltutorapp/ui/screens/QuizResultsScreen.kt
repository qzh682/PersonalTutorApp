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

@Composable
fun QuizResultsScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    val course = viewModel.getCourseById(courseId)
    val quizResults by produceState(initialValue = emptyList<MainViewModel.QuizResult>(), courseId) {
        value = viewModel.getQuizResults(courseId)
    }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(quizResults) {
        isLoading = false
    }

    Surface(modifier = Modifier.fillMaxSize()) {
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
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Quiz Results for ${course?.title ?: "Course"}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics {
                        testTag = "quiz_results_title"
                        contentDescription = "Quiz results for course ${course?.title ?: "Course"}"
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
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
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