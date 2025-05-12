package com.example.personaltutorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.personaltutorapp.navigation.NavRoutes
import com.example.personaltutorapp.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: MainViewModel
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics {
                    testTag = "login_title"
                    contentDescription = "Login title"
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "email_field"
                                contentDescription = "Email input"
                            },
                        isError = email.isBlank() && errorMessage != null
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                testTag = "password_field"
                                contentDescription = "Password input"
                            },
                        isError = password.isBlank() && errorMessage != null
                    )
                }
            }

            errorMessage?.let {
                Snackbar(
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
                    action = {
                        TextButton(onClick = { errorMessage = null }) { Text("Dismiss") }
                    }
                ) { Text(it) }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank()) {
                        errorMessage = "Please enter an email"
                        println("Validation failed: Email is blank")
                        return@Button
                    }
                    if (password.isBlank()) {
                        errorMessage = "Please enter a password"
                        println("Validation failed: Password is blank")
                        return@Button
                    }
                    isLoading = true
                    coroutineScope.launch {
                        viewModel.login(email, password) { result ->
                            isLoading = false
                            result.onSuccess { user ->
                                println("Login successful for user: ${user.email}")
                                val destination = if (user.role == "Tutor") {
                                    NavRoutes.TutorDashboard.route
                                } else {
                                    NavRoutes.StudentDashboard.route
                                }
                                navController.navigate(destination) {
                                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                                }
                            }.onFailure { e ->
                                errorMessage = "Failed to login: ${e.message}"
                                println("Login failed for $email: ${e.message}")
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .semantics {
                        testTag = "login_button"
                        contentDescription = "Login button"
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
                    Text("Login")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { navController.navigate(NavRoutes.Register.route) },
                modifier = Modifier.semantics {
                    testTag = "register_button"
                    contentDescription = "Register link"
                }
            ) {
                Text("Don't have an account? Register here")
            }
        }
    }
}