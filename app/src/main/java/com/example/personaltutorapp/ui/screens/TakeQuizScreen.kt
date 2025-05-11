package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.ui.theme.DarkGray
import com.example.personaltutorapp.ui.theme.LightGreen
import com.example.personaltutorapp.ui.theme.Purple40
import com.example.personaltutorapp.ui.theme.White
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
    var currentIndex by rememberSaveable { mutableIntStateOf(0) } // 使用 mutableIntStateOf 和 rememberSaveable
    val selectedAnswers = remember { mutableStateListOf<Int?>() }
    var showResult by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(quiz) {
        selectedAnswers.clear()
        quiz?.questions?.forEach { _ -> selectedAnswers.add(null) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background // 使用 White
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (quiz == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No quiz available",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground, // 使用 DarkGray
                        modifier = Modifier.semantics {
                            testTag = "no_quiz_text"
                            contentDescription = "No quiz available"
                        }
                    )
                }
            } else {
                if (showResult) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "quiz_result_card"
                                contentDescription = "Quiz result"
                            },
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface // 使用 LightGreen
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "You scored: $score / ${quiz.questions.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface, // 使用 DarkGray
                                modifier = Modifier.semantics {
                                    testTag = "quiz_score"
                                    contentDescription = "Score: $score out of ${quiz.questions.size}"
                                }
                            )
                        }
                    }

                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .semantics {
                                testTag = "back_to_course_button"
                                contentDescription = "Back to course"
                            },
                        shape = MaterialTheme.shapes.medium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text("Back to Course")
                    }
                } else {
                    val question = quiz.questions.getOrNull(currentIndex)
                    if (question != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    testTag = "question_card_${currentIndex}"
                                    contentDescription = "Question ${currentIndex + 1}"
                                },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface // 使用 LightGreen
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Question ${currentIndex + 1} of ${quiz.questions.size}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface, // 使用 DarkGray
                                    modifier = Modifier.semantics {
                                        testTag = "question_number_${currentIndex}"
                                        contentDescription = "Question ${currentIndex + 1} of ${quiz.questions.size}"
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = question.question,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface, // 使用 DarkGray
                                    modifier = Modifier.semantics {
                                        testTag = "question_text_${currentIndex}"
                                        contentDescription = "Question: ${question.question}"
                                    }
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                question.options.forEachIndexed { index, option ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { selectedAnswers[currentIndex] = index }
                                            .semantics {
                                                testTag = "option_${index}_${currentIndex}"
                                                contentDescription = "Option: $option"
                                            },
                                        shape = MaterialTheme.shapes.small,
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (selectedAnswers[currentIndex] == index)
                                                Purple40.copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.surface // 使用 LightGreen
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(12.dp)
                                        ) {
                                            RadioButton(
                                                selected = selectedAnswers[currentIndex] == index,
                                                onClick = { selectedAnswers[currentIndex] = index },
                                                modifier = Modifier.semantics {
                                                    testTag = "radio_${index}_${currentIndex}"
                                                    contentDescription = "Select option $option"
                                                },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Purple40,
                                                    unselectedColor = DarkGray
                                                )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = option,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface, // 使用 DarkGray
                                                modifier = Modifier.semantics {
                                                    testTag = "option_text_${index}_${currentIndex}"
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
                                    TextButton(
                                        onClick = { errorMessage = null },
                                        modifier = Modifier.semantics {
                                            testTag = "dismiss_snackbar"
                                            contentDescription = "Dismiss error message"
                                        }
                                    ) {
                                        Text("Dismiss")
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.surfaceVariant, // 使用 LightGreen
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant // 使用 DarkGray
                            ) {
                                Text(it)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (currentIndex > 0) {
                                OutlinedButton(
                                    onClick = { currentIndex-- },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .semantics {
                                            testTag = "previous_button_${currentIndex}"
                                            contentDescription = "Previous question"
                                        },
                                    shape = MaterialTheme.shapes.medium,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Purple40
                                    )
                                ) {
                                    Text("Previous")
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (selectedAnswers[currentIndex] == null) {
                                        errorMessage = "Please select an answer"
                                        return@Button
                                    }
                                    if (currentIndex < quiz.questions.size - 1) {
                                        currentIndex++
                                        errorMessage = null
                                    } else {
                                        isLoading = true
                                        val correctCount = quiz.questions.mapIndexed { i, q ->
                                            if (selectedAnswers[i] == q.correctAnswerIndex) 1 else 0
                                        }.sum()
                                        score = correctCount

                                        scope.launch {
                                            currentUser?.let { user ->
                                                viewModel.submitQuizResult(
                                                    courseId = courseId,
                                                    userId = user.id,
                                                    score = correctCount
                                                )
                                            } ?: run {
                                                errorMessage = "User not logged in, please try again"
                                            }
                                            isLoading = false
                                            showResult = true
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .semantics {
                                        testTag = if (currentIndex < quiz.questions.size - 1) "next_button_${currentIndex}" else "submit_button"
                                        contentDescription = if (currentIndex < quiz.questions.size - 1) "Next question" else "Submit quiz"
                                    },
                                shape = MaterialTheme.shapes.medium,
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple40,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    Text(if (currentIndex < quiz.questions.size - 1) "Next" else "Submit Quiz")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}