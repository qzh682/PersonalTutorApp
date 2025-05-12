package com.example.personaltutorapp.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.compose.*
import com.example.personaltutorapp.ui.screens.*
import com.example.personaltutorapp.viewmodel.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel, navController: NavHostController = rememberNavController()) {
    val currentUser by viewModel.currentUser.collectAsState()

    NavHost(navController = navController, startDestination = NavRoutes.Login.route) {

        composable(NavRoutes.Login.route) {
            LoginScreen(navController, viewModel)
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(navController, viewModel)
        }

        composable(NavRoutes.StudentDashboard.route) {
            if (currentUser == null) {
                println("User not logged in, redirecting to login")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else if (currentUser?.role != "Student") {
                println("User ${currentUser?.email} is not a student, redirecting to tutor dashboard")
                navController.navigate(NavRoutes.TutorDashboard.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                StudentDashboard(navController, viewModel)
            }
        }

        composable(NavRoutes.TutorDashboard.route) {
            if (currentUser == null) {
                println("User not logged in, redirecting to login")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else if (currentUser?.role != "Tutor") {
                println("User ${currentUser?.email} is not a tutor, redirecting to student dashboard")
                navController.navigate(NavRoutes.StudentDashboard.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                TutorDashboard(navController, viewModel)
            }
        }

        composable(NavRoutes.CreateCourse.route) {
            if (currentUser == null || currentUser?.role != "Tutor") {
                println("User ${currentUser?.email} is not a tutor or not logged in, redirecting to login")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                CreateCourseScreen(navController, viewModel)
            }
        }

        composable(
            route = NavRoutes.CourseDetail.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId.isNullOrEmpty()) {
                println("Invalid course ID for CourseDetail: $courseId")
                ErrorScreen(
                    message = "Invalid course ID",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else {
                CourseDetailScreen(courseId, navController, viewModel)
            }
        }

        composable(
            route = NavRoutes.AddLesson.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId.isNullOrEmpty()) {
                println("Invalid course ID for AddLesson: $courseId")
                ErrorScreen(
                    message = "Invalid course ID",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else if (currentUser == null || currentUser?.role != "Tutor") {
                println("User ${currentUser?.email} is not a tutor or not logged in, redirecting to login")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                AddLessonScreen(courseId = courseId, navController = navController, viewModel = viewModel)
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
            if (courseId.isNullOrEmpty() || lessonId.isNullOrEmpty()) {
                println("Invalid parameters for LessonDetail: courseId=$courseId, lessonId=$lessonId")
                ErrorScreen(
                    message = "Invalid lesson or course ID",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else {
                LessonDetailScreen(courseId, lessonId, navController, viewModel)
            }
        }

        // 保留第一个版本的 Quiz 相关部分
        composable(
            route = NavRoutes.TakeQuiz.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId.isNullOrEmpty()) {
                println("ERROR: Invalid course ID for TakeQuiz: $courseId")
                ErrorScreen(
                    message = "Invalid course ID for quiz",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else if (currentUser == null) {
                println("INFO: User not logged in, redirecting to login from TakeQuiz")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else if (currentUser?.role != "Student") {
                println("INFO: User ${currentUser?.email} is not a student, redirecting to TutorDashboard from TakeQuiz")
                navController.navigate(NavRoutes.TutorDashboard.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                TakeQuizScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            }
        }

        composable(
            route = NavRoutes.QuizResults.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId.isNullOrEmpty()) {
                println("ERROR: Invalid course ID for QuizResults: $courseId")
                ErrorScreen(
                    message = "Invalid course ID for results",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else if (currentUser == null || currentUser?.role != "Tutor") {
                println("INFO: User ${currentUser?.email} is not a tutor or not logged in, redirecting to login from QuizResults")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                QuizResultsScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            }
        }

        composable(
            route = NavRoutes.AddQuiz.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId")
            if (courseId.isNullOrEmpty()) {
                println("ERROR: Invalid course ID for AddQuiz: $courseId")
                ErrorScreen(
                    message = "Invalid course ID for adding quiz",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else if (currentUser == null || currentUser?.role != "Tutor") {
                println("INFO: User ${currentUser?.email} is not a tutor or not logged in, redirecting to login from AddQuiz")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                AddQuizScreen(courseId = courseId, navController = navController, viewModel = viewModel)
            }
        }

        // 保留第二个版本的表格日历部分
        composable(
            route = NavRoutes.TutorAvailability.route,
            arguments = listOf(navArgument("tutorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getString("tutorId")
            if (tutorId.isNullOrEmpty()) {
                println("Invalid tutor ID for TutorAvailability: $tutorId")
                ErrorScreen(
                    message = "Invalid tutor ID",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else if (currentUser == null || currentUser?.role != "Tutor") {
                println("User ${currentUser?.email} is not a tutor or not logged in, redirecting to login")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                println("DEBUG: Navigating to TutorAvailabilityScreen with tutorId=$tutorId")
                TutorAvailabilityScreen(tutorId = tutorId, navController = navController, mainViewModel = viewModel)
            }
        }

        composable(
            route = NavRoutes.StudentBooking.route,
            arguments = listOf(
                navArgument("tutorId") { type = NavType.StringType },
                navArgument("studentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getString("tutorId")
            val studentId = backStackEntry.arguments?.getString("studentId")
            if (tutorId.isNullOrEmpty() || studentId.isNullOrEmpty()) {
                println("Invalid parameters for StudentBooking: tutorId=$tutorId, studentId=$studentId")
                ErrorScreen(
                    message = "Invalid tutor or student ID",
                    navController = navController,
                    onRetry = { navController.popBackStack() }
                )
            } else if (currentUser == null || currentUser?.role != "Student") {
                println("User ${currentUser?.email} is not a student or not logged in, redirecting to login")
                navController.navigate(NavRoutes.Login.route) {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            } else {
                println("DEBUG: Navigating to StudentBookingScreen with tutorId=$tutorId, studentId=$studentId")
                StudentBookingScreen(
                    tutorId = tutorId,
                    studentId = studentId,
                    navController = navController,
                    mainViewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    message: String,
    navController: NavHostController,
    onRetry: () -> Unit = { navController.popBackStack() }
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Error",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.semantics {
                    testTag = "error_title"
                    contentDescription = "Error title"
                }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.semantics {
                    testTag = "error_message"
                    contentDescription = "Error message: $message"
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.semantics {
                        testTag = "retry_button"
                        contentDescription = "Retry"
                    }
                ) {
                    Text("Retry")
                }
                Button(
                    onClick = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    modifier = Modifier.semantics {
                        testTag = "back_to_login_button"
                        contentDescription = "Back to login"
                    }
                ) {
                    Text("Back to Login")
                }
            }
        }
    }
}