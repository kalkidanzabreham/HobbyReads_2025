package com.example.hobbyreads.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Book
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
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTopBar(
    title: String,
    navController: NavController,
    currentScreen: Screen
) {
    var authViewModel : AuthViewModel = hiltViewModel()
    Surface(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),

    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logo and title
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "HobbyReads Logo",
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation links
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            if (currentScreen != Screen.AdminDashboard) {
                                navController.navigate(Screen.AdminDashboard.route) {
                                    popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (currentScreen == Screen.AdminDashboard)
                                Purple80
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Dashboard",
                            fontWeight = if (currentScreen == Screen.AdminDashboard)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    }

                    TextButton(
                        onClick = {
                            if (currentScreen != Screen.Users) {
                                navController.navigate(Screen.Users.route) {
                                    popUpTo(Screen.Dashboard.route)
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (currentScreen == Screen.Users)
                                Purple80
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Users",
                            fontWeight = if (currentScreen == Screen.Users)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    }

                    TextButton(
                        onClick = {
                            if (currentScreen != Screen.Hobbies) {
                                navController.navigate(Screen.Hobbies.route) {
                                    popUpTo(Screen.Dashboard.route)
                                }
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = if (currentScreen == Screen.Hobbies)
                                Purple80
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Text(
                            text = "Hobbies",
                            fontWeight = if (currentScreen == Screen.Hobbies)
                                FontWeight.Bold
                            else
                                FontWeight.Normal
                        )
                    }
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
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = Color.LightGray
            )

        }
    }
}
