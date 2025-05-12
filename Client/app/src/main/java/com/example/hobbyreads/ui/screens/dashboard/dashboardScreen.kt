package com.example.hobbyreads.ui.screens.dashboard

import android.R.attr.thickness
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.hobbyreads.R
import com.example.hobbyreads.data.model.Book
import com.example.hobbyreads.data.repository.AuthRepository
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel
import com.example.hobbyreads.ui.viewmodel.BookViewModel
import com.example.hobbyreads.ui.viewmodel.ConnectionViewModel
import com.example.hobbyreads.ui.viewmodel.TradeViewModel
import com.example.hobbyreads.util.Resource
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(navController: NavController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val bookViewModel: BookViewModel = hiltViewModel()
    val connectionViewModel: ConnectionViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsState()
    val myBooksState by bookViewModel.myBooks.collectAsState()
    val count by connectionViewModel.connectionsCount.collectAsState()
    val tradeViewModel: TradeViewModel = hiltViewModel()
    val requestCount by tradeViewModel.incomingRequestCount.collectAsState()


    // Fetch books when the screen is first displayed
    LaunchedEffect(key1 = true) {
        bookViewModel.fetchMyBooks()
    }
    val myBooks = when (myBooksState) {
        is Resource.Success -> (myBooksState as Resource.Success<List<Book>>).data ?: emptyList()
        else -> emptyList()
    }
    Scaffold(
        topBar = {
            DashboardTopBar(
                userInitial = currentUser?.username?.firstOrNull()?.toString() ?: "U",
                userProfileImageUri = currentUser?.profilePicture?.let { Uri.parse(it) }, 
                onBooksClick = {
                    navController.navigate(Screen.Books.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onConnectionsClick = { navController.navigate(Screen.Connections.route) },
                onProfileIconClick = { navController.navigate(Screen.Profile.route) },
                isAdmin = if (currentUser?.isAdmin == true) "Admin" else "Not Admin",
                onAdminClick = { navController.navigate(Screen.AdminDashboard.route) }
            )
        } ,
        bottomBar = { Footer() }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Welcome section

            Text(
                text = "Welcome back, ${currentUser?.username ?: "User"}!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Here's what's happening with your reading community.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Light
            )


            // My Books Section
            DashboardCard(
                title = "My Books",
                count = if (myBooks.isEmpty()) "0" else myBooks.size.toString(),
                subtitle = if (myBooks.isEmpty()) "You have no books yet" else "books in your collection",
                painter = painterResource(id = R.drawable.logo),
                buttonText = "View All",
                onButtonClick = { navController.navigate(Screen.Books.route) }
            )


            // Connections Section
            DashboardCard(
                title = "Connections",
                count = count.toString(), // Replace with actual data
                subtitle = "people connected with you",
                painter = painterResource(id = R.drawable.person),
                buttonText = "View All",
                onButtonClick = { navController.navigate(Screen.Connections.route) }
            )
//
            // Trade Requests Section
            DashboardCard(
                title = "Trade Requests",
                count = requestCount.toString(), // Replace with actual data
                subtitle = "pending requests",
                painter = painterResource(id = R.drawable.persons),
                buttonText = "Respond",
                onButtonClick = { navController.navigate(Screen.Trades.route)}
            )
        }
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    userInitial: String,
    userProfileImageUri: Uri? = null, // Add user profile image URI
    onBooksClick: () -> Unit,
    onConnectionsClick: () -> Unit,
    onProfileIconClick: () -> Unit,
    isAdmin: String,
    onAdminClick: () -> Unit
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

                    if(isAdmin == "Admin"){
                        Text(
                            text = "Admin",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onAdminClick() }
                        )
                    }
                    else{
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
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

@Composable
fun DashboardCard(
    title: String,
    count: String,
    subtitle: String,
    painter: Painter, // Change here
    buttonText: String,
    onButtonClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .border(
                BorderStroke(0.5.dp, Color.LightGray),
                shape = RoundedCornerShape(4.dp)
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),



    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card header with title and image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium
                )

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp) // Adjust size as needed
                )
            }

            // Count and subtitle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = count,
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = onButtonClick,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple80,      

                    ),
                ) {
                    Text(text = buttonText)
                }
            }


        }
    }
}
@Composable
fun Footer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "© ${java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)} HobbyReads. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    // This is just for preview purposes
    val mockNavController = rememberNavController()
    // We can't provide a real AuthRepository in the preview
    // So we'll just show the UI with placeholder data
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Preview only shows UI, not real data", style = MaterialTheme.typography.bodySmall)
            // We can't actually render the full screen in preview due to the AuthRepository dependency
        }
    }
}
