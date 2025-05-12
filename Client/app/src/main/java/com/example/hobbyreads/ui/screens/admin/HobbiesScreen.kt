package com.example.hobbyreads.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.Hobby
import com.example.hobbyreads.ui.components.AdminTopBar
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HobbiesScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val hobbies by adminViewModel.hobbies.collectAsState()
    val isLoading by adminViewModel.isLoading.collectAsState()
    val error by adminViewModel.error.collectAsState()
    val toastMessage by adminViewModel.toastMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddHobbyDialog by remember { mutableStateOf(false) }
    var showEditHobbyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var selectedHobby by remember { mutableStateOf<Hobby?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                adminViewModel.clearToastMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "Admin",
                navController = navController,
                currentScreen = Screen.Hobbies
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
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "Hobby Management",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage hobby options for users to select",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Hobbies section with Add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hobbies",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "Manage  the list of available hobbies ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp).width(160.dp)
                    )
                }


                Button(
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple80,      

                    ),
                    onClick = { showAddHobbyDialog = true },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "Delete User",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Unspecified 
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Hobby")
                }
            }



            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search hobbies...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(
                    BorderStroke(0.5.dp, Color.LightGray),

            ),
            )

            // Hobbies list
            when (hobbies) {
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
                                text = "Error loading hobbies",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { adminViewModel.fetchHobbies() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                is Resource.Success -> {
                    val hobbyList = (hobbies as Resource.Success<List<Hobby>>).data
                    val filteredHobbies = if (searchQuery.isBlank()) {
                        hobbyList
                    } else {
                        hobbyList.filter { it.name.contains(searchQuery, ignoreCase = true) }
                    }

                    if (filteredHobbies.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No hobbies found" else "No matching hobbies found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ID",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Light,
                                modifier = Modifier.width(40.dp)
                            )
                            Text(
                                text = "Name",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Light,
                                modifier = Modifier.weight(1f)
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
                        // Hobby list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            itemsIndexed(filteredHobbies) { index, hobby ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp, horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${hobby.id}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(40.dp)
                                    )
                                    Text(
                                        text = hobby.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Row(
                                        modifier = Modifier.width(80.dp),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                selectedHobby = hobby
                                                showEditHobbyDialog = true
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                selectedHobby = hobby
                                                showDeleteConfirmDialog = true
                                            },modifier = Modifier.width(60.dp)
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.delete_icon), 
                                                contentDescription = "Delete User",
                                                tint = Color.Unspecified 
                                            )
                                        }
                                    }
                                }

                                if (index < filteredHobbies.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Hobby Dialog
    if (showAddHobbyDialog) {
        AddHobbyDialog(
            onDismiss = { showAddHobbyDialog = false },
            onAddHobby = { hobbyName ->
                adminViewModel.createHobby(hobbyName)
                showAddHobbyDialog = false
            },
            isLoading = isLoading
        )
    }

    // Edit Hobby Dialog
    if (showEditHobbyDialog && selectedHobby != null) {
        EditHobbyDialog(
            hobby = selectedHobby!!,
            onDismiss = { showEditHobbyDialog = false },
            onUpdateHobby = { id, name ->
                adminViewModel.updateHobby(id, name)
                showEditHobbyDialog = false
            },
            isLoading = isLoading
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog && selectedHobby != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Hobby") },
            text = { Text("Are you sure you want to delete the hobby '${selectedHobby!!.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        adminViewModel.deleteHobby(selectedHobby!!.id)
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AddHobbyDialog(
    onDismiss: () -> Unit,
    onAddHobby: (String) -> Unit,
    isLoading: Boolean
) {
    var hobbyName by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "HobbyReads Logo",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Add New Hobby",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Create a new hobby for users to select",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = hobbyName,
                    onValueChange = { hobbyName = it },
                    label = { Text("Hobby Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (hobbyName.isNotBlank()) {
                                keyboardController?.hide()
                                onAddHobby(hobbyName)
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple80,      

                    ),
                    onClick = { onAddHobby(hobbyName) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hobbyName.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Add Hobby")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun EditHobbyDialog(
    hobby: Hobby,
    onDismiss: () -> Unit,
    onUpdateHobby: (Int, String) -> Unit,
    isLoading: Boolean
) {
    var hobbyName by remember { mutableStateOf(hobby.name) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Edit Hobby",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Update hobby information",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = hobbyName,
                    onValueChange = { hobbyName = it },
                    label = { Text("Hobby Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (hobbyName.isNotBlank()) {
                                keyboardController?.hide()
                                onUpdateHobby(hobby.id, hobbyName)
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple80,      

                        ),
                        onClick = { onUpdateHobby(hobby.id, hobbyName) },
                        modifier = Modifier.weight(1f),
                        enabled = hobbyName.isNotBlank() && !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Update")
                        }
                    }
                }
            }
        }
    }
}


