package com.example.hobbyreads.ui.screens.books

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hobbyreads.ui.components.LoadingIndicator
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.screens.dashboard.Footer
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.ui.viewmodel.BookViewModel
import com.example.hobbyreads.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBookScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val bookViewModel: BookViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val error by bookViewModel.error.collectAsState()
    val addBookStatus by bookViewModel.addBookStatus.collectAsState()
    val bookAdded by remember { derivedStateOf { bookViewModel.bookAdded } }

    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("Fiction") }
    var condition by remember { mutableStateOf("Good") }
    var status by remember { mutableStateOf("Available for Trade") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // For dropdown menus
    var genreExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }
    var statusExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Image picker
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    // Success state observer
    LaunchedEffect(bookAdded) {
        if (bookAdded) {
            navController.navigate(Screen.Books.route) {
                popUpTo(Screen.Books.route) { inclusive = true }
            }
            bookViewModel.resetBookAdded()
        }
    }

    Scaffold(
        topBar = {
            DashboardTopBookBar(
                userInitial = currentUser?.username?.firstOrNull()?.toString() ?: "U",
                userProfileImageUri = currentUser?.profilePicture?.let { Uri.parse(it) }, // ✅ Fix here
                onDashboardClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onConnectionsClick = { navController.navigate(Screen.Connections.route) },
                onProfileIconClick = { navController.navigate(Screen.Profile.route) }

            )
        },
        bottomBar = { Footer() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // Header
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Add New Book",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Enter the details of the book you want to add",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Main Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
            ) {
                Column(
//                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // Title
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Title*",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Enter book title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Author
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Author*",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = author,
                            onValueChange = { author = it },
                            placeholder = { Text("Enter author name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Description",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Enter a brief description of the book") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            maxLines = 4,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    // Genre and Condition
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Genre
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Genre*",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            ExposedDropdownMenuBox(
                                expanded = genreExpanded,
                                onExpandedChange = { genreExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = genre,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("Select genre") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = genreExpanded,
                                    onDismissRequest = { genreExpanded = false }
                                ) {
                                    listOf(
                                        "Fiction", "Science Fiction", "Fantasy", "Mystery",
                                        "Thriller", "Romance", "Biography", "History",
                                        "Self-Help", "Poetry", "Historical Fiction"
                                    ).forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                genre = option
                                                genreExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Condition
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Condition*",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            ExposedDropdownMenuBox(
                                expanded = conditionExpanded,
                                onExpandedChange = { conditionExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = condition,
                                    onValueChange = { },
                                    readOnly = true,
                                    placeholder = { Text("Select condition") },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded)
                                    },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = conditionExpanded,
                                    onDismissRequest = { conditionExpanded = false }
                                ) {
                                    listOf(
                                        "Like New", "Excellent", "Very Good",
                                        "Good", "Fair", "Poor"
                                    ).forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                condition = option
                                                conditionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

//                    Trade status

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Trade Status*",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        ExposedDropdownMenuBox(
                            expanded = statusExpanded,
                            onExpandedChange = { statusExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = status,
                                onValueChange = { }, // keep as is because it's read-only dropdown
                                readOnly = true,
                                placeholder = { Text("Select trade status") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = statusExpanded,
                                onDismissRequest = { statusExpanded = false }
                            ) {
                                listOf(
                                    "Available for Trade",
                                    "Not for Trade"
                                ).forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            status = option  // ✅ updates the selected status
                                            statusExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Cover Image
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Cover Image",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        OutlinedButton(
                            onClick = { launcher.launch("image/*") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Cover Image",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image")
                        }
                        Text(
                            text = "Optional. Maximum file size: 5MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Show selected image preview
                        selectedImageUri?.let { uri ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selected image: ${uri.lastPathSegment}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    // Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { navController.navigateUp() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple80,      // 🔥 Background color

                            ),
                            onClick = {
                                bookViewModel.addBook(
                                    title = title,
                                    author = author,
                                    description = description,
                                    genre = genre,
                                    condition = condition,
                                    status = status,
                                    coverImageUri = selectedImageUri
                                )
                            },
                            modifier = Modifier.weight(1f),
                            enabled = title.isNotBlank() && author.isNotBlank() &&
                                    genre.isNotBlank() && condition.isNotBlank() &&
                                    status.isNotBlank() && !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Adding Book...")
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Book")
                            }
                        }
                    }

                    // Error message
                    error?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

