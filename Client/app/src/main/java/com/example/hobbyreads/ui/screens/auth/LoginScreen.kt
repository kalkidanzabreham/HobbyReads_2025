package com.example.hobbyreads.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.hobbyreads.R
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple40
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.util.Resource

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val loginState by viewModel.loginState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    // If user is already logged in, navigate to dashboard
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Handle login state changes
    LaunchedEffect(loginState) {
        when (loginState) {
            is Resource.Success -> {
                if (currentUser?.isAdmin == true) {
                    navController.navigate(Screen.AdminDashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                } else {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            }
            is Resource.Error -> {
                errorMessage = (loginState as Resource.Error).message
            }
            else -> {}
        }
    }


    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Logo and App Name
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically // ✅ Align image and text in center vertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "HobbyReads Logo",
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(8.dp)) // ✅ Add space between image and text

            Text(
                text = "HobbyReads",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Login Title
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(32.dp))


        Text(
            text = "Enter your username and password to access your account",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Error message
        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Forgot Password
        TextButton(
            onClick = { /* Navigate to forgot password */ },
            modifier = Modifier.align(Alignment.End),
            enabled = !isLoading
        ) {
            Text(text = "Forgot password?",
                color = Purple80)
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                    )
                }
            },
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))



        // Login Button
        Button(
            onClick = {
                errorMessage = null

                // Validate inputs
                when {
                    username.isBlank() -> errorMessage = "Please enter your username"
                    password.isBlank() -> errorMessage = "Please enter your password"
                    else -> {
                        viewModel.login(username, password, navController as NavHostController)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isLoading ,
            colors = ButtonDefaults.buttonColors(
                    containerColor = Purple80,      // 🔥 Background color

        ),
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Up Link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = {
                    viewModel.resetLoginState()
                    navController.navigate(Screen.Register.route)
                },
                enabled = !isLoading
            ) {
                Text(text = "Sign up",
                    color = Purple80)
            }
        }
    }
}
