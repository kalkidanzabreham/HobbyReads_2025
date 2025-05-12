package com.example.hobbyreads.ui.screens.books

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.Book
import com.example.hobbyreads.ui.components.BookCard
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.screens.dashboard.DashboardTopBar
import com.example.hobbyreads.ui.screens.dashboard.Footer
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.ui.viewmodel.BookViewModel
import com.example.hobbyreads.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BooksScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val bookViewModel: BookViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val booksResource by bookViewModel.books.collectAsState()
    val isLoading by bookViewModel.isLoading.collectAsState()
    val error by bookViewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("") }
    var genreFilter by remember { mutableStateOf("") }

    // Fetch books when the screen is first displayed
    LaunchedEffect(key1 = true) {
        bookViewModel.fetchBooks()
    }
    val books = when (booksResource) {
        is Resource.Success -> (booksResource as Resource.Success<List<Book>>).data ?: emptyList()
        else -> emptyList()
    }

//     Filter books based on search query and filters
    val filteredBooks = books.filter { book ->
        val matchesSearch = searchQuery.isEmpty() ||
                book.title.contains(searchQuery, ignoreCase = true) ||
                book.author.contains(searchQuery, ignoreCase = true)

        val matchesStatus = statusFilter.isEmpty() || book.status == statusFilter
        val matchesGenre = genreFilter.isEmpty() || book.genre == genreFilter

        matchesSearch && matchesStatus && matchesGenre
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddBook.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Book")
            }
        },
        bottomBar = { Footer() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Books",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Browse and manage your book collection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple80,      // 🔥 Background color

                        ),
                        onClick = { navController.navigate(Screen.AddBook.route) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Book")
                    }
                }


            }

            // Search & Filter Card
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by title or author...") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    singleLine = true,

                    shape = RoundedCornerShape(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))



            // Books Grid
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Error: ${error ?: "Unknown error"}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { bookViewModel.fetchBooks() }) {
                                Text("Retry")
                            }
                        }
                    }
                }
                filteredBooks.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "No books found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Try adjusting your search or filter, or add a new book.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Purple80,      // 🔥 Background color

                                ),
                                onClick = { navController.navigate(Screen.AddBook.route) }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Book")
                            }
                        }
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(filteredBooks) { book ->
                            BookCard(
                                book = book,
                                onClick = { navController.navigate(Screen.BookDetail.route + "/${book.id}") }
                            )
                        }
                    }
                }
            }
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBookBar(
    userInitial: String,
    userProfileImageUri: Uri? = null, // Add user profile image URI
    onDashboardClick: () -> Unit,
    onConnectionsClick: () -> Unit,
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

                    )

                    Text(
                        text = "Connections",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { onConnectionsClick() }
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
