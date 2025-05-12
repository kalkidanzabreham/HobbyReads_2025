package com.example.hobbyreads.ui.screens.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.Hobby
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // State for profile editing
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var selectedHobbies by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }



    // Load URI from SharedPreferences
    LaunchedEffect(key1 = Unit) {
        val userId = currentUser?.id
        val key = "profile_picture_uri_$userId"

        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val uriString = sharedPreferences.getString(key, null)
        val uri = uriString?.let { Uri.parse(it) }
        selectedImageUri = uri
    }
    fun clearProfilePictureUri(context: Context, userId: String) {
        val key = "profile_picture_uri_$userId"
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(key).apply()
        selectedImageUri = null 
    }



    LaunchedEffect(profileState.isSuccess) {
        if (profileState.isSuccess) {
            navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Profile.route) { inclusive = true }
            }
        }
    }

    // All available hobbies
    val allHobbies = listOf(
        "Fiction", "Non-Fiction", "Science Fiction", "Fantasy", "Mystery",
        "Romance", "Biography", "History", "Self-Help", "Poetry",
        "Comics", "Art", "Historical Fiction", "Book Clubs"
    )

    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it

         
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            
            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("profile_picture_uri", it.toString()).apply()
        }
    }

    // Initialize form with current user data
    LaunchedEffect(currentUser) {
        currentUser?.let {
            name = it.name.toString()
            bio = it.bio ?: ""
            selectedHobbies = it.hobbies.map { hobby -> hobby.name }
        }
    }

    // Handle profile update success/error
    LaunchedEffect(profileState) {
        if (profileState.isSuccess) {
            scope.launch {
                snackbarHostState.showSnackbar("Profile updated successfully")
                profileViewModel.resetProfileState()
            }
        } else if (profileState.error != null) {
            scope.launch {
                snackbarHostState.showSnackbar("Error: ${profileState.error}")
                profileViewModel.resetProfileState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { clearProfilePictureUri(context,
                        currentUser?.id.toString()
                    )
                        selectedImageUri = null
                        navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Dashboard.route) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your profile information.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Your Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "How others see you on HobbyReads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        
                        val profileImageUrl = selectedImageUri?.toString() ?: currentUser?.profilePicture

                        if (profileImageUrl != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(profileImageUrl)
                                    .crossfade(true)
                                    .error(R.drawable.hero) 
                                    .build(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                           
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape)
                                    .background(color =Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.firstOrNull()?.toString() ?: "?",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple80,      

                            ),
                            onClick = {
                               
                                launcher.launch(arrayOf("image/*"))
                            }
                        ) {
                            Text("Change Profile Picture")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name and Username
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "@${currentUser?.username ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Bio
                        if (bio.isNotBlank()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = bio,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }




                        // Hobbies
                        if (selectedHobbies.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(0.8f),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    HobbyChipGroup(hobbies = selectedHobbies)
                                }
                            }
                        }
                    }
                }
            }

            // Edit Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Edit Profile",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Update your profile information",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Name Input
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            placeholder = { Text("Your name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Bio Input
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            placeholder = { Text("Tell others about yourself and your reading interests") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Hobbies Section
                        Text(
                            text = "Hobbies",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Selected Hobbies
                        if (selectedHobbies.isNotEmpty()) {
                            HobbyChipGroup(
                                hobbies = selectedHobbies,
                                onRemoveHobby = { hobby ->
                                    selectedHobbies = selectedHobbies.filter { it != hobby }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Available Hobbies Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(allHobbies.filter { it !in selectedHobbies }) { hobby ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedHobbies = selectedHobbies + hobby
                                        },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = false,
                                        onCheckedChange = {
                                            selectedHobbies = selectedHobbies + hobby
                                        }
                                    )
                                    Text(
                                        text = hobby,
                                        style = MaterialTheme.typography.bodyMedium,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Profile Picture Upload
                        Text(
                            text = "Profile Picture",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = { launcher.launch(arrayOf("image/*")) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Upload Profile Picture"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Select Image")
                        }

                        Text(
                            text = "Optional. Maximum file size: 5MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Show selected image name
                        selectedImageUri?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Selected image: ${it.lastPathSegment}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Save Button
                        Button(
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple80,      

                            ),
                            onClick = {
                                profileViewModel.updateProfile(
                                    name = name,
                                    bio = bio,
                                    hobbies = selectedHobbies,
                                    profilePicture = selectedImageUri
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading && name.isNotBlank()
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Saving...")
                            } else {
                                Text("Save Changes")
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Logout Button
                        OutlinedButton(
                            onClick = {
                                clearProfilePictureUri(context, currentUser?.id.toString())
                                selectedImageUri = null

                                authViewModel.logout()
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.Dashboard.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Logout"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HobbyChipGroup(
    hobbies: List<String>,
    onRemoveHobby: ((String) -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            hobbies.forEach { hobby ->
                Surface(
                    modifier = Modifier.padding(4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = hobby,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )

                        if (onRemoveHobby != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove $hobby",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable { onRemoveHobby(hobby) },
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable () -> Unit
) {
    Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val rowSpacing = 4.dp.roundToPx()
        val itemSpacing = 4.dp.roundToPx()

        // Keep track of width and height of each row
        val rows = mutableListOf<MutableList<Pair<Int, Int>>>()
        var currentRow = mutableListOf<Pair<Int, Int>>()
        var currentRowWidth = 0

        // Measure each child
        val placeables = measurables.map { measurable ->
            val placeable = measurable.measure(constraints.copy(minWidth = 0))

            // If this item doesn't fit in the current row, start a new row
            if (currentRowWidth + placeable.width > constraints.maxWidth && currentRow.isNotEmpty()) {
                rows.add(currentRow)
                currentRow = mutableListOf()
                currentRowWidth = 0
            }

            currentRow.add(Pair(placeable.width, placeable.height))
            currentRowWidth += placeable.width + itemSpacing

            placeable
        }

        // Add the last row if it's not empty
        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        // Calculate the layout height based on the rows
        val height = rows.sumOf { row -> row.maxOfOrNull { it.second } ?: 0 } +
                (rows.size - 1) * rowSpacing

        // Place the children
        layout(constraints.maxWidth, height) {
            var y = 0
            var itemIndex = 0

            rows.forEach { row ->
                var x = 0
                val rowHeight = row.maxOfOrNull { it.second } ?: 0

                row.forEach { (_, _) ->
                    val placeable = placeables[itemIndex++]
                    placeable.placeRelative(x, y)
                    x += placeable.width + itemSpacing
                }

                y += rowHeight + rowSpacing
            }
        }
    }
}
