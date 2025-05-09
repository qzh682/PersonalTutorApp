package com.example.personaltutorapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.personaltutorapp.ui.screens.*
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel, navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = NavRoutes.Login.route) {

        composable(NavRoutes.Login.route) {
            LoginScreen(navController = navController, viewModel = viewModel)
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(navController = navController, viewModel = viewModel)
        }

        composable(NavRoutes.StudentDashboard.route) {
            StudentDashboard(navController = navController, viewModel = viewModel)
        }

        composable(NavRoutes.TutorDashboard.route) {
            TutorDashboard(navController = navController, viewModel = viewModel)
        }

        composable(NavRoutes.CreateCourse.route) {
            CreateCourseScreen(navController = navController, viewModel = viewModel)
        }

        composable(
            route = NavRoutes.CourseDetail.route + "/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CourseDetailScreen(courseId = courseId, navController = navController, viewModel = viewModel)
        }

        composable(
            route = NavRoutes.AddLesson.route + "/{courseId}",
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            AddLessonScreen(courseId = courseId, navController = navController, viewModel = viewModel)
        }

        composable(
            route = NavRoutes.LessonDetail.route + "/{courseId}/{lessonId}",
            arguments = listOf(
                navArgument("courseId") { type = NavType.StringType },
                navArgument("lessonId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: return@composable
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: return@composable
            LessonDetailScreen(courseId = courseId, lessonId = lessonId, navController = navController, viewModel = viewModel)
        }
    }
}
