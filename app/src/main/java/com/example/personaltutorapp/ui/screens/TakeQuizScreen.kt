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
import kotlinx.coroutines.launch

@Composable
fun TakeQuizScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    val course = viewModel.getCourseById(courseId)
    val quiz = course?.quiz
    val currentUser by viewModel.currentUser.collectAsState()
    // Use a mutableStateMapOf to ensure recompositions on updates
    val answers = remember { mutableStateMapOf<Int, Int>() }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) }
    var submittedScore by remember { mutableStateOf<Int?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Debug log to confirm screen is loaded
    LaunchedEffect(Unit) {
        println("INFO: TakeQuizScreen loaded for courseId: $courseId, quiz: ${quiz?.id}")
        if (currentUser != null && quiz != null) {
            // Check if the user has already submitted a result
            val submissions = viewModel.getQuizResults(courseId)
            val userSubmission = submissions.find { it.studentName == currentUser!!.displayName }
            if (userSubmission != null) {
                hasSubmitted = true
                submittedScore = userSubmission.score
                println("INFO: User ${currentUser!!.id} has already submitted a result for quiz ${quiz.id}: Score=${userSubmission.score}/${userSubmission.total}")
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Quiz for ${course?.title ?: "Course"}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "quiz_title"
                    contentDescription = "Quiz for ${course?.title ?: "Course"}"
                }
            )

            if (quiz == null || !quiz.isPublished) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (quiz == null) "No quiz available" else "Quiz not published",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            testTag = "no_quiz_text"
                            contentDescription = if (quiz == null) "No quiz available" else "Quiz not published"
                        }
                    )
                }
            } else if (currentUser == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please log in to take the quiz",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            testTag = "no_user_text"
                            contentDescription = "Please log in to take the quiz"
                        }
                    )
                }
            } else if (hasSubmitted) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "You have already submitted this quiz",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.semantics {
                                testTag = "already_submitted_text"
                                contentDescription = "Already submitted message"
                            }
                        )
                        submittedScore?.let { score ->
                            Text(
                                text = "Your Score: $score / ${quiz.questions.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.semantics {
                                    testTag = "submitted_score"
                                    contentDescription = "Your score: $score out of ${quiz.questions.size}"
                                }
                            )
                        }
                        Button(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.semantics {
                                testTag = "back_to_dashboard_button"
                                contentDescription = "Back to dashboard"
                            }
                        ) {
                            Text("Back to Dashboard")
                        }
                    }
                }
            } else {
                quiz.questions.forEachIndexed { index, question ->
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .semantics {
                                testTag = "question_card_$index"
                                contentDescription = "Question ${index + 1}"
                            },
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Question ${index + 1}: ${question.question}",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.semantics {
                                    testTag = "question_text_$index"
                                    contentDescription = "Question ${index + 1}: ${question.question}"
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row {
                                question.options.forEachIndexed { optionIndex, option ->
                                    Row(
                                        modifier = Modifier
                                            .padding(end = 16.dp)
                                            .semantics {
                                                testTag = "answer_option_${index}_$optionIndex"
                                                contentDescription = "Select $option for question ${index + 1}"
                                            }
                                    ) {
                                        RadioButton(
                                            selected = answers[index] == optionIndex,
                                            onClick = {
                                                answers[index] = optionIndex
                                                println("INFO: Selected option $optionIndex for question $index")
                                            },
                                            modifier = Modifier.semantics {
                                                testTag = "radio_${index}_$optionIndex"
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = option,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (answers.size != quiz.questions.size) {
                            errorMessage = "Please answer all questions"
                            println("ERROR: Validation failed: Not all questions answered")
                            return@Button
                        }
                        isSubmitting = true
                        errorMessage = null
                        val score = quiz.questions.mapIndexed { index, question ->
                            if (answers[index] == question.correctAnswerIndex) 1 else 0
                        }.sum()
                        coroutineScope.launch {
                            viewModel.submitQuizResult(courseId, currentUser!!.id, score) { result ->
                                isSubmitting = false
                                result.onSuccess {
                                    hasSubmitted = true
                                    submittedScore = score
                                    println("INFO: Quiz result submitted: Score=$score for user ${currentUser!!.id}")
                                    // Do not navigate back immediately to show the score
                                }.onFailure { e ->
                                    errorMessage = "Failed to submit quiz: ${e.message}"
                                    println("ERROR: Failed to submit quiz: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics {
                            testTag = "submit_quiz_button"
                            contentDescription = "Submit quiz"
                        },
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isSubmitting
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Submit Quiz")
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
            }
        }
    }
}