package com.example.hobbyreads.ui.screens.trades

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.TradeRequest
import com.example.hobbyreads.data.model.TradeStatus
import com.example.hobbyreads.data.model.TradeType
import com.example.hobbyreads.ui.components.LoadingIndicator
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.ui.viewmodel.BookViewModel
import com.example.hobbyreads.ui.viewmodel.TradeViewModel
import com.example.hobbyreads.util.Resource
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradesScreen(
    navController: NavController,
    tradeViewModel: TradeViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val bookViewModel: BookViewModel = hiltViewModel()
    val tradeRequests by tradeViewModel.tradeRequests.collectAsState()
    val isLoading by tradeViewModel.isLoading.collectAsState()
    val error by tradeViewModel.error.collectAsState()
    val toastMessage by tradeViewModel.toastMessage.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Incoming Requests", "Outgoing Requests", "Accepted Requests")

    // Log the tradeRequests when fetched
    LaunchedEffect(tradeRequests) {
        when (tradeRequests) {
            is Resource.Loading -> Log.d("TradesScreen", "Fetching trade requests...")
            is Resource.Error -> Log.d("TradesScreen", "Error fetching trade requests: ${(tradeRequests as Resource.Error).message}")
            is Resource.Success -> Log.d("TradesScreen", "Fetched trade requests: ${(tradeRequests as Resource.Success<List<TradeRequest>>).data}")
        }
    }

    LaunchedEffect(Unit) {
        tradeViewModel.fetchTradeRequests()
    }

    // Show toast message
    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                tradeViewModel.clearToastMessage()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trade Requests") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
                    text = "Book Trade Requests",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Manage your incoming and outgoing trade requests",
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
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab content
            when (tradeRequests) {
                is Resource.Loading -> {
                    LoadingIndicator()
                }
                is Resource.Error -> {
                    ErrorCard(message = (tradeRequests as Resource.Error).message)
                }
                is Resource.Success -> {
                    val requests = (tradeRequests as Resource.Success<List<TradeRequest>>).data
                    val incomingRequests = requests.filter { it.type == TradeType.INCOMING }
                    val outgoingRequests = requests.filter { it.type == TradeType.OUTGOING }

                    when (selectedTab) {
                        0 -> IncomingRequestsTab(
                            requests = incomingRequests,
                            onAccept = { tradeId, bookId ->
                                tradeViewModel.updateTradeRequestStatus(tradeId, TradeStatus.ACCEPTED)
                                bookViewModel.fetchBookById(bookId)
                            },
                            onReject = { tradeId ->
                                tradeViewModel.updateTradeRequestStatus(tradeId, TradeStatus.REJECTED)
                            },
                            isLoading = isLoading,
                            navController = navController
                        )

                        1 -> OutgoingRequestsTab(
                            requests = outgoingRequests,
                            onCancel = { tradeViewModel.updateTradeRequestStatus(it, TradeStatus.CANCELLED) },
                            isLoading = isLoading,
                            navController = navController
                        )

                        2 -> AcceptedRequestsTab(
                            userId = currentUser?.id.toString(), // Pass the userId here
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun IncomingRequestsTab(
    requests: List<TradeRequest>,
    onAccept: (tradeId: Int, bookId: Int) -> Unit,
    onReject: (tradeId: Int) -> Unit,
    isLoading: Boolean,
    navController: NavController
) {
    if (requests.isEmpty()) {
        EmptyStateCard(
            title = "No Incoming Requests",
            description = "You don't have any incoming trade requests at the moment."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(requests) { request ->
                TradeRequestCard(
                    tradeRequest = request,
                    actions = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onReject(request.id) },
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
                                onClick = { onAccept(request.id, request.bookId)  },
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
                    onBookClick = { navController.navigate(Screen.BookDetail.createRoute(request.bookId.toString())) },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun OutgoingRequestsTab(
    requests: List<TradeRequest>,
    onCancel: (Int) -> Unit,
    isLoading: Boolean,
    navController: NavController
) {
    if (requests.isEmpty()) {
        EmptyStateCard(
            title = "No Outgoing Requests",
            description = "You haven't sent any trade requests yet."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(requests) { request ->
                TradeRequestCard(
                    tradeRequest = request,
                    actions = {
                        Button(
                            onClick = { onCancel(request.id) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            enabled = !isLoading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cancel Request")
                        }
                    },
                    onBookClick = { navController.navigate(Screen.BookDetail.createRoute(request.bookId.toString())) },
                    isLoading = isLoading
                )
            }
        }
    }
}

@Composable
fun TradeRequestCard(
    tradeRequest: TradeRequest,
    actions: @Composable () -> Unit,
    onBookClick: () -> Unit,
    isLoading: Boolean
) {
    val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
        ) {
            // Request header with user info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // User avatar
                val user = if (tradeRequest.type == TradeType.INCOMING) tradeRequest.requester else tradeRequest.owner
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color = Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.first().toString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Request info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (tradeRequest.type == TradeType.INCOMING)
                            "${tradeRequest.requester.name} wants to trade"
                        else
                            "You requested from ${tradeRequest.owner.name}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Requested on ${dateFormatter.format(tradeRequest.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Book info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBookClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Book cover
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(tradeRequest.book.coverImage ?: R.drawable.hero)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Cover of ${tradeRequest.book.title}",
                    modifier = Modifier
                        .size(80.dp, 120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    fallback = painterResource(id = R.drawable.hero)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tradeRequest.book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "by ${tradeRequest.book.author}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tradeRequest.book.genre.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tradeRequest.book.bookCondition.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Message if any
            if (!tradeRequest.message.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Message:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tradeRequest.message,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                actions()
            }
        }
    }
}

@Composable
fun AcceptedRequestsTab(
    userId: String, // UserId is passed here
    navController: NavController,
    tradeViewModel: TradeViewModel = hiltViewModel() // ViewModel to fetch the data
) {
    // Trigger fetching the accepted requests when the screen is loaded
    LaunchedEffect(userId) {
        tradeViewModel.loadAcceptedRequests(userId)
    }

    // Collect the acceptedRequests StateFlow from the ViewModel
    val acceptedRequests by tradeViewModel.acceptedRequests.collectAsState()

    if (acceptedRequests.isEmpty()) {
        EmptyStateCard(
            title = "No Accepted Requests",
            description = "You don't have any accepted trade requests yet."
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(acceptedRequests) { request ->
                TradeRequestCard(
                    tradeRequest = request,
                    actions = {}, // No actions on accepted requests
                    onBookClick = {},
                    isLoading = false
                )
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
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "HobbyReads Logo",
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
