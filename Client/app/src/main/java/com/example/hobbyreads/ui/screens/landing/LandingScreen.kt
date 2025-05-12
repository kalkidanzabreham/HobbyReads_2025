package com.example.hobbyreads.ui.screens.landing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.hobbyreads.ui.navigation.Screen
import com.example.hobbyreads.ui.theme.HobbyTheme
import com.example.hobbyreads.ui.theme.Purple40
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.hobbyreads.R

@Composable
fun LandingScreen(navController: NavController) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Header(
            onLoginClick = { navController.navigate(Screen.Login.route) },
            onSignUpClick = { navController.navigate(Screen.Register.route) }
        )

        // Hero Section
        HeroSection(
            onGetStartedClick = { navController.navigate(Screen.Register.route) },
            onSignInClick = { navController.navigate(Screen.Login.route) }
        )

        // Features Section
        FeaturesSection()

        // Footer
        Footer()
    }
}

@Composable
fun Header(
    onLoginClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Logo and App Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "HobbyReads Logo",
                    modifier = Modifier.size(32.dp)
                )

                Text(
                    text = "HobbyReads",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.W900
                )
            }

            // Login and Sign Up Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onLoginClick) {
                    Text("Login")
                }
                Button(onClick = onSignUpClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White,

                        ),shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.width(100.dp)
                        .height(32.dp).offset(y= (8).dp)) {
                    Text(text = "Sign Up",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun HeroSection(
    onGetStartedClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Hero Text
        Text(
            text = "Connect Through Books",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Start,
            modifier = Modifier.offset(x= (-24).dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Share your reading journey, discover new books, and connect with people who share your reading interests.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // CTA Buttons
        Row(
            modifier = Modifier
                .width(300.dp).offset(x= (-16).dp)
            ,
            horizontalArrangement = Arrangement.Start // ✅ Align buttons at the start (left)
        ) {
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .width(160.dp)
                    .height(40.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Get Started")
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onSignInClick,
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sign In")
            }
        }




        Spacer(modifier = Modifier.height(32.dp))

        // Hero Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        ) {
            // In a real app, you would load an actual image here
            // For now, we'll use a placeholder
            Image(
                painter = painterResource(id = R.drawable.hero),
                contentDescription = "HobbyReads App",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Features",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Everything you need to share your reading journey and connect with others.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Feature 1: Book Exchange
        FeatureItem(
            icon = Icons.Default.Book,
            title = "Book Exchange",
            description = "List books you've read or are willing to trade with others in your community."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feature 2: Hobby-Based Matching
        FeatureItem(
            icon = Icons.Default.People,
            title = "Hobby-Based Matching",
            description = "Connect with people who share your hobbies and reading interests."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Feature 3: Book Reviews
        FeatureItem(
            icon = Icons.Default.Person,
            title = "Book Reviews",
            description = "Share your thoughts on books and discover what others think."
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = Purple40,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
fun LandingScreenPreview() {
    HobbyTheme {
        LandingScreen(navController = rememberNavController())
    }
}
