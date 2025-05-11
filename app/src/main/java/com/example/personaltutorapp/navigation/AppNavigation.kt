package com.example.personaltutorapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personaltutorapp.ui.screens.*
import com.example.personaltutorapp.viewmodel.MainViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.ui.Alignment

@Composable
fun AppNavigation(viewModel: MainViewModel, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NavRoutes.Login.route) {

        composable(NavRoutes.Login.route) {
            LoginScreen(navController, viewModel)
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(navController, viewModel)
        }

        composable(NavRoutes.StudentDashboard.route) {
            StudentDashboard(navController, viewModel)
        }

        composable(NavRoutes.TutorDashboard.route) {
            TutorDashboard(navController, viewModel)
        }

        composable(NavRoutes.CreateCourse.route) {
            CreateCourseScreen(navController, viewModel)
        }

        composable(
            route = NavRoutes.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (!courseId.isNullOrEmpty()) {
                CourseDetailScreen(courseId, navController, viewModel)
            } else {
                ErrorScreen("Invalid course ID", navController)
            }
        }

        composable(
            route = NavRoutes.AddLesson.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (!courseId.isNullOrEmpty()) {
                AddLessonScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            } else {
                ErrorScreen("Invalid course ID", navController)
            }
        }

        composable(
            route = NavRoutes.LessonDetail.route,
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            val lessonId = backStackEntry.arguments?.getString("lessonId")
            if (!courseId.isNullOrEmpty() && !lessonId.isNullOrEmpty()) {
                LessonDetailScreen(courseId, lessonId, navController, viewModel)
            } else {
                ErrorScreen("Invalid lesson or course ID", navController)
            }
        }

        composable(
            route = NavRoutes.Quiz.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (!courseId.isNullOrEmpty()) {
                QuizScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            } else {
                ErrorScreen("Invalid course ID for quiz", navController)
            }
        }

        composable(
            route = NavRoutes.TakeQuiz.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (!courseId.isNullOrEmpty()) {
                TakeQuizScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            } else {
                ErrorScreen("Invalid course ID for quiz", navController)
            }
        }

        composable(
            route = NavRoutes.QuizResults.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (!courseId.isNullOrEmpty()) {
                QuizResultsScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            } else {
                ErrorScreen("Invalid course ID for results", navController)
            }
        }
    }
}

@Composable
fun ErrorScreen(message: String, navController: NavHostController) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(message, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }
}
