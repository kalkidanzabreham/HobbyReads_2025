package com.example.hobbyreads.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.User
import com.example.hobbyreads.ui.components.AdminTopBar
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AdminViewModel
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.util.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val users by adminViewModel.users.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val error by adminViewModel.error.collectAsState()
    val toastMessage by adminViewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    var showDeleteDialog by remember { mutableStateOf(false) }
    var userIdToDelete by remember { mutableStateOf<String?>(null) }

    val deleteStatus by adminViewModel.deleteUserStatus.collectAsState()

    LaunchedEffect(deleteStatus) {
        when (deleteStatus) {
            is Resource.Success -> {
                adminViewModel.fetchUsers()
            }

            is Resource.Error -> adminViewModel.fetchUsers()
            Resource.Loading -> adminViewModel.fetchUsers()
        }
    }




    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            adminViewModel.clearToastMessage()
        }
    }

    LaunchedEffect(Unit) {
        adminViewModel.fetchUsers()
    }


    Scaffold(
        topBar = {
            AdminTopBar(
                title = "Admin",
                navController = navController,
                currentScreen = Screen.Users
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header
            Column(modifier = Modifier.padding(bottom = 16.dp,top = 16.dp)) {
                Text(
                    text = "User Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "View and manage user accounts",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray
                )

            }
            Column(modifier = Modifier.padding(16.dp)){
                Text(
                    text = "Users",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage user accounts and permissions",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))


                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(text = "Search users by name, username, email...",style = MaterialTheme.typography.bodySmall, fontWeight =FontWeight.Light,) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp).border(
                            BorderStroke(0.5.dp, Color.LightGray),
                        ),
//                    singleLine = true,
//                    shape = RoundedCornerShape(8.dp)
                )
            }


            when (users) {
                is Resource.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error loading users",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { adminViewModel.fetchUsers() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    val userList = (users as Resource.Success<List<User>>).data
                    val filteredUsers = if (searchQuery.isBlank()) {
                        userList
                    } else {
                        userList.filter {
                            it.name?.contains(searchQuery, ignoreCase = true) == true ||
                                    it.username.contains(searchQuery, ignoreCase = true) ||
                                    it.email.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No users found" else "No matching users found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // 🌐 Horizontally scrollable table
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .horizontalScroll(rememberScrollState())
                        ) {
                            // Table header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Username",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    text = "Name",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.width(120.dp)
                                )
                                Text(
                                    text = "Email",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.width(200.dp)
                                )
                                Text(
                                    text = "Role",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.width(80.dp)
                                )
                                Text(
                                    text = "Joined",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.width(100.dp)
                                )
                                Text(
                                    text = "Actions",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Light,
                                    modifier = Modifier.width(60.dp)
                                )
                            }

                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = Color.LightGray
                            )

                            LazyColumn {
                                itemsIndexed(filteredUsers) { index, user ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "@${user.username}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.width(120.dp),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = user.name.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.width(120.dp),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.width(200.dp),
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        // Role (colored)
                                        Text(
                                            text = if (user.isAdmin) "Admin" else "User",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = if (user.isAdmin)
                                                    Purple80
                                                else
                                                    MaterialTheme.colorScheme.onSurface,
                                                fontWeight = if (user.isAdmin) FontWeight.Bold else FontWeight.Normal
                                            ),
                                            modifier = Modifier.width(80.dp)
                                        )
                                        // Joined date
                                        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                                        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

                                        val outputFormat = SimpleDateFormat("M/d/yyyy", Locale.getDefault())

                                        val parsedDate = try {
                                            inputFormat.parse(user.createdAt)
                                        } catch (e: Exception) {
                                            null
                                        }

                                        Text(
                                            text = parsedDate?.let { outputFormat.format(it) } ?: "Invalid date",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.width(100.dp)
                                        )


                                        IconButton(
                                            onClick = {
                                                userIdToDelete = user.id.toString()  // Set the selected user id
                                                showDeleteDialog = true   // Show dialog
                                            },
                                            modifier = Modifier.width(60.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.delete_icon), 
                                                contentDescription = "Delete User",
                                                tint = Color.Unspecified 
                                            )
                                        }


                                    }
                                    HorizontalDivider(
                                        thickness = 0.5.dp,
                                        color = Color.LightGray
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text(text = "Confirm Delete")
            },
            text = {
                Text("Are you sure you want to delete this user?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        userIdToDelete?.let { id ->
                            if (id == currentUser?.id.toString()) {
                               
                                adminViewModel.deleteOwnAccount(
                                    password = "passwordHere", // TODO: Get password from user
                                    onResult = { success ->
                                        if (success) {
                                            
                                            navController.navigate(Screen.Login.route) {
                                                popUpTo(Screen.Users.route) { inclusive = true }
                                            }
                                        }
                                    }
                                )
                            } else {
                                
                                adminViewModel.deleteUserById(id)

                            }
                        }
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }


}

