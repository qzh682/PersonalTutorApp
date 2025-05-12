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
import com.example.personaltutorapp.model.QuizQuestion
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@Composable
fun TakeQuizScreen(
    courseId: String,
    navController: NavController,
    viewModel: MainViewModel
) {
    val course = viewModel.getCourseById(courseId)
    val quiz = course?.quiz?.takeIf { it.isPublished } // Only show published quizzes
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (quiz == null || currentUser == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when {
                            currentUser == null -> "Please login to take the quiz"
                            course?.quiz == null -> "No quiz available for this course"
                            else -> "Quiz is not published yet"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            testTag = "no_quiz_text"
                            contentDescription = when {
                                currentUser == null -> "Please login to take the quiz"
                                course?.quiz == null -> "No quiz available"
                                else -> "Quiz is not published yet"
                            }
                        }
                    )
                }
            } else {
                currentUser?.let { user ->
                    val submission = quiz.submissions.find { it.userId == user.id }
                    var showQuiz by remember { mutableStateOf(submission == null) }
                    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
                    val selectedAnswers = remember { mutableStateListOf<Int?>() }
                    var score by remember { mutableStateOf(0) }
                    var completed by remember { mutableStateOf(false) }
                    var showAnswers by remember { mutableStateOf(false) }

                    LaunchedEffect(showQuiz) {
                        if (showQuiz) {
                            selectedAnswers.clear()
                            quiz.questions.forEach { _ -> selectedAnswers.add(null) }
                            currentIndex = 0
                            completed = false
                            score = 0
                            showAnswers = false
                            println("Initialized quiz: ${quiz.questions.size} questions")
                        }
                    }

                    if (!showQuiz && submission != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    testTag = "submission_card"
                                    contentDescription = "Quiz submission details"
                                },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "You've already completed this quiz",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.semantics {
                                        testTag = "submission_status"
                                        contentDescription = "Quiz already completed"
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Score: ${submission.score} / ${quiz.questions.size}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.semantics {
                                        testTag = "submission_score"
                                        contentDescription = "Score: ${submission.score} out of ${quiz.questions.size}"
                                    }
                                )
                            }
                        }

                        Button(
                            onClick = { showQuiz = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics {
                                    testTag = "retake_quiz_button"
                                    contentDescription = "Retake quiz"
                                },
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Retake Quiz")
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
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text("Back to Course")
                        }
                    } else if (!completed) {
                        val question = quiz.questions.getOrNull(currentIndex)
                        if (question != null) {
                            // Progress bar
                            LinearProgressIndicator(
                                progress = (currentIndex + 1) / quiz.questions.size.toFloat(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics {
                                        testTag = "progress_bar"
                                        contentDescription = "Progress: ${currentIndex + 1} of ${quiz.questions.size}"
                                    }
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            QuestionCard(
                                question = question,
                                currentIndex = currentIndex,
                                selectedAnswer = selectedAnswers[currentIndex],
                                onAnswerSelected = { selectedAnswers[currentIndex] = it }
                            )

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
                                    }
                                ) { Text(it) }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (currentIndex > 0) {
                                    OutlinedButton(
                                        onClick = {
                                            currentIndex--
                                            errorMessage = null
                                            println("Moved to previous question: $currentIndex")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .semantics {
                                                testTag = "previous_button_${currentIndex}"
                                                contentDescription = "Previous question"
                                            },
                                        shape = MaterialTheme.shapes.medium
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
                                            println("Validation failed: Answer not selected for question ${currentIndex + 1}")
                                            return@Button
                                        }
                                        if (currentIndex < quiz.questions.size - 1) {
                                            currentIndex++
                                            errorMessage = null
                                            println("Moved to next question: ${currentIndex + 1}")
                                        } else {
                                            isLoading = true
                                            score = quiz.questions.mapIndexed { i, q ->
                                                if (selectedAnswers[i] == q.correctAnswerIndex) 1 else 0
                                            }.sum()

                                            scope.launch {
                                                viewModel.submitQuizResult(courseId, user.id, score) { result ->
                                                    isLoading = false
                                                    result.onSuccess {
                                                        println("Quiz submitted successfully: Score=$score for user ${user.id}")
                                                        completed = true
                                                    }.onFailure { e ->
                                                        errorMessage = "Failed to submit quiz: ${e.message}"
                                                        println("Failed to submit quiz for user ${user.id}: ${e.message}")
                                                    }
                                                }
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
                                    enabled = !isLoading
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else {
                                        Text(if (currentIndex < quiz.questions.size - 1) "Next" else "Submit")
                                    }
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    testTag = "quiz_result_card"
                                    contentDescription = "Quiz result"
                                },
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Quiz Completed!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.semantics {
                                        testTag = "quiz_completed_text"
                                        contentDescription = "Quiz completed"
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You scored $score out of ${quiz.questions.size}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.semantics {
                                        testTag = "quiz_score"
                                        contentDescription = "Score: $score out of ${quiz.questions.size}"
                                    }
                                )
                            }
                        }

                        if (showAnswers) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                quiz.questions.forEachIndexed { index, question ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .semantics {
                                                testTag = "answer_review_card_$index"
                                                contentDescription = "Answer review for question ${index + 1}"
                                            },
                                        shape = MaterialTheme.shapes.medium,
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text(
                                                text = "Question ${index + 1}: ${question.question}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.semantics {
                                                    testTag = "answer_review_question_$index"
                                                    contentDescription = "Question ${index + 1}"
                                                }
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Your answer: ${selectedAnswers[index]?.let { question.options[it] } ?: "None"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (selectedAnswers[index] == question.correctAnswerIndex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.semantics {
                                                    testTag = "your_answer_$index"
                                                    contentDescription = "Your answer for question ${index + 1}"
                                                }
                                            )
                                            Text(
                                                text = "Correct answer: ${question.options[question.correctAnswerIndex]}",
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.semantics {
                                                    testTag = "correct_answer_$index"
                                                    contentDescription = "Correct answer for question ${index + 1}"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showAnswers = !showAnswers },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .semantics {
                                        testTag = "toggle_answers_button"
                                        contentDescription = if (showAnswers) "Hide answers" else "Show answers"
                                    },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(if (showAnswers) "Hide Answers" else "Show Answers")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .semantics {
                                        testTag = "back_to_course_button_completed"
                                        contentDescription = "Back to course"
                                    },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text("Back to Course")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionCard(
    question: QuizQuestion,
    currentIndex: Int,
    selectedAnswer: Int?,
    onAnswerSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                testTag = "question_card_${currentIndex}"
                contentDescription = "Question ${currentIndex + 1}"
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Question ${currentIndex + 1}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics {
                    testTag = "question_number_${currentIndex}"
                    contentDescription = "Question ${currentIndex + 1}"
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyLarge,
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
                        .clickable { onAnswerSelected(index) }
                        .semantics {
                            testTag = "option_${index}_${currentIndex}"
                            contentDescription = "Option: $option"
                        },
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedAnswer == index)
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(12.dp)
                    ) {
                        RadioButton(
                            selected = selectedAnswer == index,
                            onClick = { onAnswerSelected(index) },
                            modifier = Modifier.semantics {
                                testTag = "radio_${index}_${currentIndex}"
                                contentDescription = "Select option $option"
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.semantics {
                                testTag = "option_text_${index}_${currentIndex}"
                            }
                        )
                    }
                }
            }
        }
    }
}