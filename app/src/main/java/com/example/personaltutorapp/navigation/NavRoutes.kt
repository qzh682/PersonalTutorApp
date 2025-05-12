package com.example.personaltutorapp.navigation

sealed class NavRoutes(val route: String) {

    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object StudentDashboard : NavRoutes("student_dashboard")
    object TutorDashboard : NavRoutes("tutor_dashboard")
    object CreateCourse : NavRoutes("create_course")

    object CourseDetail : NavRoutes("course_detail/{courseId}") {
        fun createRoute(courseId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            return "course_detail/$courseId"
        }
    }

    object AddLesson : NavRoutes("add_lesson/{courseId}") {
        fun createRoute(courseId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            return "add_lesson/$courseId"
        }
    }

    object LessonDetail : NavRoutes("lesson_detail/{courseId}/{lessonId}") {
        fun createRoute(courseId: String, lessonId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            require(lessonId.isNotBlank()) { "Lesson ID cannot be blank" }
            return "lesson_detail/$courseId/$lessonId"
        }
    }

    object TakeQuiz : NavRoutes("take_quiz/{courseId}") {
        fun createRoute(courseId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            return "take_quiz/$courseId"
        }
    }

    object QuizResults : NavRoutes("quiz_results/{courseId}") {
        fun createRoute(courseId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            return "quiz_results/$courseId"
        }
    }

    // Add the missing AddQuiz route
    object AddQuiz : NavRoutes("add_quiz/{courseId}") {
        fun createRoute(courseId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            return "add_quiz/$courseId"
        }
    }
}