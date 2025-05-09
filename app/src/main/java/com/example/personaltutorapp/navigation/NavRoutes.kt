package com.example.personaltutorapp.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")
    object Register : NavRoutes("register")
    object StudentDashboard : NavRoutes("student_dashboard")
    object TutorDashboard : NavRoutes("tutor_dashboard")
    object CreateCourse : NavRoutes("create_course")

    object CourseDetail : NavRoutes("course_detail/{courseId}") {
        fun createRoute(courseId: String) = "course_detail/$courseId"
    }

    object AddLesson : NavRoutes("add_lesson/{courseId}") {
        fun createRoute(courseId: String) = "add_lesson/$courseId"
    }

    object LessonDetail : NavRoutes("lesson_detail/{courseId}/{lessonId}") {
        fun createRoute(courseId: String, lessonId: String) = "lesson_detail/$courseId/$lessonId"
    }
}
