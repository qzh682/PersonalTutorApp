package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import java.util.Random
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuizScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    // Debug log to confirm screen is loaded
    LaunchedEffect(Unit) {
        println("AddQuizScreen loaded for courseId: $courseId")
    }

    val course = viewModel.getCourseById(courseId)
    var questionText by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }
    var optionC by remember { mutableStateOf("") }
    var optionD by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("A") }
    val questions = remember { mutableStateListOf<QuizQuestion>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }
    var editingQuestionIndex by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Automatically generate addition questions when the screen loads
    LaunchedEffect(course) {
        if (course != null && questions.isEmpty()) {
            println("Generating addition questions for course $courseId with ${course.lessons.size} lessons")
            val random = Random()
            course.lessons.forEach { lesson ->
                repeat(1) { // 1 question per lesson
                    val num1 = random.nextInt(50) + 1 // Random number between 1 and 50
                    val num2 = random.nextInt(50) + 1 // Random number between 1 and 50
                    val correctSum = num1 + num2
                    val question = "What is $num1 + $num2 for Lesson ${lesson.title}?"

                    // Generate incorrect options
                    val incorrectOptions = mutableListOf<Int>()
                    while (incorrectOptions.size < 3) {
                        val incorrectSum = correctSum + random.nextInt(10) - 5 // Generate a number close to the correct sum
                        if (incorrectSum != correctSum && incorrectSum > 0 && incorrectSum !in incorrectOptions) {
                            incorrectOptions.add(incorrectSum)
                        }
                    }

                    // Randomly place the correct answer among the options
                    val options = mutableListOf(correctSum, incorrectOptions[0], incorrectOptions[1], incorrectOptions[2])
                    options.shuffle()
                    val correctAnswerIndex = options.indexOf(correctSum)

                    val newQuestion = QuizQuestion(
                        id = UUID.randomUUID().toString(),
                        question = question,
                        options = options.map { it.toString() },
                        correctAnswerIndex = correctAnswerIndex
                    )
                    questions.add(newQuestion)
                    println("Generated question: $question, Correct answer: $correctSum")
                }
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (editingQuestionIndex == null) "Add Quiz" else "Edit Question",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "add_quiz_title"
                    contentDescription = if (editingQuestionIndex == null) "Add quiz title" else "Edit question title"
                }
            )

            // If course or lessons are not available, show a message
            if (course == null || course.lessons.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (course == null) "Course not found" else "No lessons available for this course",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.semantics {
                            testTag = "no_lessons_text"
                            contentDescription = if (course == null) "Course not found" else "No lessons available"
                        }
                    )
                }
            } else {
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
                                },
                            isError = questionText.isBlank() && errorMessage != null
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
                                },
                            isError = optionA.isBlank() && errorMessage != null
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
                                },
                            isError = optionB.isBlank() && errorMessage != null
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
                                },
                            isError = optionC.isBlank() && errorMessage != null
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
                                },
                            isError = optionD.isBlank() && errorMessage != null
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

                // 显示已添加的问题预览（删除 "Questions Added" 文本）
                if (questions.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            questions.forEachIndexed { index, question ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Question ${index + 1}: ${question.question.take(30)}${if (question.question.length > 30) "..." else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier
                                            .weight(1f)
                                            .semantics {
                                                testTag = "question_preview_$index"
                                                contentDescription = "Question ${index + 1} preview"
                                            }
                                    )
                                    Row {
                                        IconButton(
                                            onClick = {
                                                questionText = question.question
                                                optionA = question.options[0]
                                                optionB = question.options[1]
                                                optionC = question.options[2]
                                                optionD = question.options[3]
                                                correctAnswer = when (question.correctAnswerIndex) {
                                                    0 -> "A"
                                                    1 -> "B"
                                                    2 -> "C"
                                                    3 -> "D"
                                                    else -> "A"
                                                }
                                                editingQuestionIndex = index
                                                println("Editing question $index: ${question.question}")
                                            },
                                            modifier = Modifier.semantics {
                                                testTag = "edit_button_$index"
                                                contentDescription = "Edit question ${index + 1}"
                                            }
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null)
                                        }
                                        IconButton(
                                            onClick = {
                                                questions.removeAt(index)
                                                println("Deleted question $index")
                                            },
                                            modifier = Modifier.semantics {
                                                testTag = "delete_button_$index"
                                                contentDescription = "Delete question ${index + 1}"
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Debug log if no questions are added
                    LaunchedEffect(Unit) {
                        println("No questions generated for courseId: $courseId")
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
                        // 验证输入
                        if (questionText.isBlank() || optionA.isBlank() || optionB.isBlank() ||
                            optionC.isBlank() || optionD.isBlank()) {
                            errorMessage = "Please fill all fields"
                            println("Validation failed: All fields must be filled")
                            return@Button
                        }

                        // 检查选项是否重复
                        val options = listOf(optionA, optionB, optionC, optionD)
                        val distinctOptions = options.distinct()
                        if (distinctOptions.size != options.size) {
                            errorMessage = "Options must be unique"
                            println("Validation failed: Options must be unique")
                            return@Button
                        }

                        val correctAnswerIndex = when (correctAnswer) {
                            "A" -> 0
                            "B" -> 1
                            "C" -> 2
                            "D" -> 3
                            else -> 0
                        }

                        try {
                            val newQuestion = QuizQuestion(
                                id = UUID.randomUUID().toString(),
                                question = questionText,
                                options = options,
                                correctAnswerIndex = correctAnswerIndex
                            )
                            if (editingQuestionIndex != null) {
                                questions[editingQuestionIndex!!] = newQuestion
                                println("Updated question $editingQuestionIndex: ${newQuestion.question}")
                                editingQuestionIndex = null
                            } else {
                                questions.add(newQuestion)
                                println("Added question: ${newQuestion.question}")
                            }

                            // 重置输入字段
                            questionText = ""
                            optionA = ""
                            optionB = ""
                            optionC = ""
                            optionD = ""
                            correctAnswer = "A"
                            errorMessage = null
                        } catch (e: IllegalArgumentException) {
                            errorMessage = "Invalid question: ${e.message}"
                            println("Failed to add/edit question: ${e.message}")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .semantics {
                            testTag = "add_question_button"
                            contentDescription = if (editingQuestionIndex == null) "Add question" else "Update question"
                        },
                    shape = MaterialTheme.shapes.medium,
                    enabled = !isLoading && !isPublishing
                ) {
                    Text(if (editingQuestionIndex == null) "Add Question" else "Update Question")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (questions.isEmpty()) {
                                errorMessage = "Please add at least one question"
                                println("Validation failed: At least one question required")
                                return@Button
                            }
                            isLoading = true
                            coroutineScope.launch {
                                viewModel.addQuizToCourse(courseId, questions) { result ->
                                    isLoading = false
                                    result.onSuccess {
                                        println("Quiz saved successfully for course $courseId with ${questions.size} questions")
                                    }.onFailure { e ->
                                        errorMessage = "Failed to save quiz: ${e.message}"
                                        println("Failed to save quiz: ${e.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics {
                                testTag = "save_quiz_button"
                                contentDescription = "Save quiz"
                            },
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading && !isPublishing
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

                    Button(
                        onClick = {
                            if (questions.isEmpty()) {
                                errorMessage = "Please add at least one question"
                                println("Validation failed: At least one question required")
                                return@Button
                            }
                            isPublishing = true
                            coroutineScope.launch {
                                viewModel.addQuizToCourse(courseId, questions) { result ->
                                    result.onSuccess {
                                        viewModel.publishQuiz(courseId) { publishResult ->
                                            isPublishing = false
                                            publishResult.onSuccess {
                                                println("Quiz published successfully for course $courseId")
                                                navController.popBackStack()
                                            }.onFailure { e ->
                                                errorMessage = "Failed to publish quiz: ${e.message}"
                                                println("Failed to publish quiz: ${e.message}")
                                            }
                                        }
                                    }.onFailure { e ->
                                        isPublishing = false
                                        errorMessage = "Failed to save quiz before publishing: ${e.message}"
                                        println("Failed to save quiz before publishing: ${e.message}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .semantics {
                                testTag = "publish_quiz_button"
                                contentDescription = "Publish quiz"
                            },
                        shape = MaterialTheme.shapes.medium,
                        enabled = !isLoading && !isPublishing
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Publish Quiz")
                        }
                    }
                }
            }
        }
    }
}