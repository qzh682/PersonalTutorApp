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

    // 保留第一个版本的 Quiz 相关部分
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

    object AddQuiz : NavRoutes("add_quiz/{courseId}") {
        fun createRoute(courseId: String): String {
            require(courseId.isNotBlank()) { "Course ID cannot be blank" }
            return "add_quiz/$courseId"
        }
    }

    // 保留第二个版本的表格日历部分
    object TutorAvailability : NavRoutes("tutor_availability/{tutorId}") {
        fun createRoute(tutorId: String): String {
            require(tutorId.isNotBlank()) { "Tutor ID cannot be blank" }
            return "tutor_availability/$tutorId"
        }
    }

    object StudentBooking : NavRoutes("student_booking/{tutorId}/{studentId}") {
        fun createRoute(tutorId: String, studentId: String): String {
            require(tutorId.isNotBlank()) { "Tutor ID cannot be blank" }
            require(studentId.isNotBlank()) { "Student ID cannot be blank" }
            return "student_booking/$tutorId/$studentId"
        }
    }
}