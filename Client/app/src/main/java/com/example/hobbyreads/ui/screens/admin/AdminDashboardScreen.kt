package com.example.hobbyreads.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.hobbyreads.R
import com.example.hobbyreads.ui.components.AdminTopBar
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    adminViewModel: AdminViewModel = hiltViewModel()
) {
    val totalUsers by adminViewModel.totalUsers.collectAsState()
    val totalHobbies by adminViewModel.totalHobbies.collectAsState()

    Scaffold(
        topBar = {
            AdminTopBar(
                title = "Admin",
                navController = navController,
                currentScreen = Screen.AdminDashboard
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = "Admin Dashboard",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Overview of platform stats",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Total Users Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp).border(
                        BorderStroke(0.5.dp, Color.LightGray),
                        shape = RoundedCornerShape(4.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                onClick = {navController.navigate(Screen.Users.route)}
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Total Users",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = totalUsers.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Image(
                        painter = painterResource(id = R.drawable.persons),
                        contentDescription = "HobbyReads Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }

            }

            // Total Hobbies Card
            Card(
                modifier = Modifier.fillMaxWidth().border(
                    BorderStroke(0.5.dp, Color.LightGray),
                    shape = RoundedCornerShape(4.dp)

                ),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                onClick = {navController.navigate(Screen.Hobbies.route)}
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Total Hobbies",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = totalHobbies.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.hobby),
                        contentDescription = "HobbyReads Logo",
                        modifier = Modifier.size(32.dp)
                    )
                }

            }
        }
    }
}


