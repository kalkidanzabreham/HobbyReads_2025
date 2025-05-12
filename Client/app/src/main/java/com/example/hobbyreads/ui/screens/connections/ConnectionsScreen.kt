package com.example.hobbyreads.ui.screens.connections

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobby.R
import com.example.hobbyreads.data.model.Connection
import com.example.hobbyreads.ui.components.LoadingIndicator
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.screens.books.DashboardTopBookBar
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.ui.viewmodel.ConnectionViewModel
import com.example.hobbyreads.util.Resource
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionsScreen(
    navController: NavController,
    connectionViewModel: ConnectionViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val connections by connectionViewModel.connections.collectAsState()
    val pendingConnections by connectionViewModel.pendingConnections.collectAsState()
    val suggestedConnections by connectionViewModel.suggestedConnections.collectAsState()
    val isLoading by connectionViewModel.isLoading.collectAsState()
    val error by connectionViewModel.error.collectAsState()
    val toastMessage by connectionViewModel.toastMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("My Connections", "Pending Requests", "Suggested Connections")

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                connectionViewModel.clearToastMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            DashboardTopConnectionBar(
                userInitial = currentUser?.username?.firstOrNull()?.toString() ?: "U",
                userProfileImageUri = currentUser?.profilePicture?.let { Uri.parse(it) }, 
                onDashboardClick = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onBooksClick = { navController.navigate(Screen.Books.route) },
                onProfileIconClick = { navController.navigate(Screen.Profile.route) }

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
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Connections",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connect with people who share your reading interests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(

                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = title, style = MaterialTheme.typography.bodySmall)
                                if (index == 1 && pendingConnections is Resource.Success) {
                                    val count = (pendingConnections as Resource.Success<List<Connection>>).data.size
                                    if (count > 0) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ) {
                                            Text(count.toString())
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab content
            when (selectedTab) {
                0 -> MyConnectionsTab(
                    connections = connections,
                    onRemoveConnection = { connectionViewModel.removeConnection(it) },
                    isLoading = isLoading
                )
                1 -> PendingRequestsTab(
                    pendingConnections = pendingConnections,
                    onAcceptConnection = { connectionViewModel.acceptConnection(it) },
                    onRejectConnection = { connectionViewModel.rejectConnection(it) },
                    isLoading = isLoading
                )
                2 -> SuggestedConnectionsTab(
                    suggestedConnections = suggestedConnections,
                    onSendRequest = { connectionViewModel.sendConnectionRequest(it) },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun MyConnectionsTab(
    connections: Resource<List<Connection>>,
    onRemoveConnection: (Int) -> Unit,
    isLoading: Boolean
) {
    when (connections) {
        is Resource.Loading -> {
            LoadingIndicator()
        }
        is Resource.Error -> {
            ErrorCard(message = connections.message)
        }
        is Resource.Success -> {
            if (connections.data.isEmpty()) {
                EmptyStateCard(
                    title = "No connections yet",
                    description = "You don't have any connections yet. Check the suggested connections tab to find people with similar interests."
                )
            } else {
                ConnectionsGrid(
                    connections = connections.data,
                    actionButton = { connection ->
                        OutlinedButton(
                            shape = RoundedCornerShape(8.dp),
                            onClick = { onRemoveConnection(connection.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Remove Connection")
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun PendingRequestsTab(
    pendingConnections: Resource<List<Connection>>,
    onAcceptConnection: (Int) -> Unit,
    onRejectConnection: (Int) -> Unit,
    isLoading: Boolean
) {
    when (pendingConnections) {
        is Resource.Loading -> {
            LoadingIndicator()
        }
        is Resource.Error -> {
            ErrorCard(message = pendingConnections.message)
        }
        is Resource.Success -> {
            if (pendingConnections.data.isEmpty()) {
                EmptyStateCard(
                    title = "No pending requests",
                    description = "You don't have any pending connection requests."
                )
            } else {
                ConnectionsGrid(
                    connections = pendingConnections.data,
                    actionButton = { connection ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onRejectConnection(connection.id) },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reject")
                            }

                            Button(
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple80,      

                                ),
                                onClick = { onAcceptConnection(connection.id) },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Accept")
                            }
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun SuggestedConnectionsTab(
    suggestedConnections: Resource<List<Connection>>,
    onSendRequest: (Int) -> Unit,
    isLoading: Boolean
) {
    when (suggestedConnections) {
        is Resource.Loading -> {
            LoadingIndicator()
        }
        is Resource.Error -> {
            ErrorCard(message = suggestedConnections.message)
        }
        is Resource.Success -> {
            if (suggestedConnections.data.isEmpty()) {
                EmptyStateCard(
                    title = "No suggested connections",
                    description = "We don't have any suggested connections for you at the moment."
                )
            } else {
                ConnectionsGrid(
                    connections = suggestedConnections.data,
                    actionButton = { connection ->
                        Button(
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple80,      // 🔥 Background color

                            ),
                            onClick = { onSendRequest(connection.userId) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Connect")
                        }
                    },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun ConnectionsGrid(
    connections: List<Connection>,
    actionButton: @Composable (Connection) -> Unit,
    isLoading: Boolean
) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 300.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(connections) { connection ->
                ConnectionCard(
                    connection = connection,
                    actionButton = { actionButton(connection) },
                    isLoading = isLoading
                )
            }
        }
    }



@Composable
fun ConnectionCard(
    connection: Connection,
    actionButton: @Composable () -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().border(
            BorderStroke(0.5.dp, Color.LightGray),
        ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),

    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header with avatar and name
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color = Color.LightGray),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = connection.name.first().toString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Name and username
                    Column {
                        Text(
                            text = connection.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "@${connection.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bio
                Text(
                    text = connection.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Hobbies
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    connection.hobbies.forEach { hobby ->
                        SuggestionChip(
                            onClick = { },
                            label = { Text(hobby, style = MaterialTheme.typography.bodySmall) },
                            shape = RoundedCornerShape(16.dp),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Match percentage
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Match:",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    LinearProgressIndicator(
                        progress = { connection.matchPercentage / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Purple80, 
                        trackColor = Color.LightGray 
                    )


                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${connection.matchPercentage}%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Action button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isLoading) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    actionButton()
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ErrorCard(
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Error",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopConnectionBar(
    userInitial: String,
    userProfileImageUri: Uri? = null, // Add user profile image URI
    onDashboardClick: () -> Unit,
    onBooksClick: () -> Unit,
    onProfileIconClick: () -> Unit,

    ) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()


    CenterAlignedTopAppBar(
        title = {
            Column {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)

                ) {
                    // Book logo
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "HobbyReads Logo",
                        modifier = Modifier.size(32.dp)
                    )


                    Text(
                        text = "Dashboard",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onDashboardClick() }
                    )

                    // Navigation text



                    Text(
                        text = "Books",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onBooksClick() }

                        )

                    Text(
                        text = "Connections",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,

                    )

                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = Color.LightGray
                )

            }

        },
        actions = {
            // User avatar (image or initial)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { onProfileIconClick() },
                contentAlignment = Alignment.Center
            ) {
                if (userProfileImageUri != null) {
                    // Display the profile image if available
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(userProfileImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Otherwise, display the user initial
                    Text(
                        text = userInitial,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    )
}
