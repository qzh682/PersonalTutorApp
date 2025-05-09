package com.example.personaltutorapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.personaltutorapp.ui.screens.*
import com.example.personaltutorapp.viewmodel.MainViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx. compose. material3.Button

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
            if (courseId != null) {
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
            if (courseId != null) {
                AddLessonScreen(courseId, navController, viewModel)
            } else {
                ErrorScreen("Invalid course ID for lesson", navController)
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
            if (courseId != null && lessonId != null) {
                LessonDetailScreen(courseId, lessonId, navController, viewModel)
            } else {
                ErrorScreen("Invalid lesson or course ID", navController)
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

// 你已经定义好的 NavRoutes 对象应如下保持一致：

