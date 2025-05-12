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
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.Hobby
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple40
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AdminViewModel
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.util.Resource
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val adminViewModel: AdminViewModel = hiltViewModel()
    val hobbiesState by adminViewModel.hobbies.collectAsState()


    val hobbies = when (hobbiesState) {
        is Resource.Success -> (hobbiesState as Resource.Success<List<Hobby>>).data ?: emptyList()
        else -> emptyList() // Show empty list while loading or error
    }

    var selectedHobbies by remember { mutableStateOf(emptyList<String>()) }

    val registerState by viewModel.registerState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Handle registration state changes
    LaunchedEffect(registerState) {
        when (registerState) {
            is Resource.Success -> {
                // Redirect to dashboard after successful registration (auto-login)
                navController.navigate(Screen.Dashboard.route) {
                    popUpTo(Screen.Register.route) { inclusive = true }
                }
            }
            is Resource.Error -> {
                errorMessage = (registerState as Resource.Error).message
            }
            else -> {}
        }
    }

    // Show a snackbar with registration result
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(registerState) {
        viewModel.registerState.collectLatest { state ->
            when (state) {
                is Resource.Success -> {
                    snackbarHostState.showSnackbar(
                        message = "Registration successful! Welcome to HobbyReads.",
                        duration = SnackbarDuration.Short
                    )
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
                else -> {}
            }
        }
    }

    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

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

            // Register Title
            Text(
                text = "Create an Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Enter your details to create a new account",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Error message
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Text(
                text = "Username",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // Username Field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Enter your username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Email",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Full Name (Optional)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            // Name Field
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Enter your full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Field

            // Password Field
            Text(
                text = "Password*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Enter your password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Field
            Text(
                text = "Confirm Password*",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp)
            )
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Enter your password") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            imageVector = if (isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password"
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Hobbies Section
            Text(
                text = "Select Your Hobbies*",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))


            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                hobbies.forEach { hobby ->
                    val hobbyName = hobby.name // Use name directly
                    val isSelected = selectedHobbies.contains(hobbyName)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                selectedHobbies = if (checked) {
                                    selectedHobbies + hobbyName // Add name
                                } else {
                                    selectedHobbies - hobbyName // Remove name
                                }
                            }
                        )
                        Text(
                            text = hobbyName, // Show name
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }






            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    // Validate inputs
                    when {
                        username.isBlank() -> errorMessage = "Please enter a username"
                        email.isBlank() -> errorMessage = "Please enter an email"
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                            errorMessage = "Please enter a valid email"
                        password.isBlank() -> errorMessage = "Please enter a password"
                        password.length < 6 -> errorMessage = "Password must be at least 6 characters"
                        password != confirmPassword -> errorMessage = "Passwords do not match"
                        selectedHobbies.isEmpty() -> errorMessage = "Please select at least one hobby"
                        else -> {
                            errorMessage = null
                            viewModel.register(
                                username = username,
                                email = email,
                                password = password,
                                name = name.takeIf { it.isNotBlank() },
                                hobbies = selectedHobbies.toList()
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
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
                    Text("Register")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Login Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = { navController.navigate(Screen.Login.route) }) {
                    Text(text = "Login",color = Purple80)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
