package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun QuizResultsScreen(courseId: String, navController: NavController, viewModel: MainViewModel) {
    val course = viewModel.getCourseById(courseId)

    val quizResults by produceState(initialValue = emptyList<MainViewModel.QuizResult>()) {
        value = viewModel.getQuizResults(courseId)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Quiz Results for ${course?.title}", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (quizResults.isEmpty()) {
                Text("No quiz results available.", style = MaterialTheme.typography.bodyMedium)
            } else {
                quizResults.forEach { result ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Student: ${result.studentName}", style = MaterialTheme.typography.titleSmall)
                            Text("Score: ${result.score} / ${result.total}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                Text("Back to Dashboard")
            }
        }
    }
}
