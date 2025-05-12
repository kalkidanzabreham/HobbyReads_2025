package com.example.hobbyreads.ui.components


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.hobbyreads.data.model.Book
import com.example.hobbyreads.ui.theme.Purple80
import com.example.hobbyreads.ui.viewmodel.TradeViewModel
import com.example.hobbyreads.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    tradeViewModel: TradeViewModel = hiltViewModel()
) {
    // Observe sent requests from ViewModel
    val sentRequests by tradeViewModel.sentRequests.collectAsState()

    // Determine if this book has a request sent
    val requestSent = book.id in sentRequests

    val imageUrl = if (book.coverImage.isNullOrBlank()) {
        null
    } else {
        Constants.Image_URL + book.coverImage
    }
    Log.d("BookCard", "Loading image URL: $imageUrl")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.7f)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        val context = LocalContext.current
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cover Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(data = imageUrl)
                            .build()
                    ),
                    contentDescription = "Cover of ${book.title}",
                    modifier = Modifier
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(4.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }



            // Book Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)

            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {

                    Text(
                        text = book.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    SuggestionChip(
                        onClick = {
                            if (!requestSent && book.status == "Available for Trade") {
                                tradeViewModel.createTradeRequest(book.id)
                            }
                        },
                        label = {
                            Text(
                                text = when {
                                    requestSent -> "Request Sent"
                                    book.status == "Available for Trade" -> "Trade"
                                    else -> "No Trade"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = when {
                                    book.status == "Available for Trade" && !requestSent -> Color.White // ✅ Text white when trade
                                    else -> Color.DarkGray // ✅ Text dark when request sent or no trade
                                }
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = when {
                                book.status == "Available for Trade" && !requestSent -> Purple80 // ✅ Purple background when available
                                else -> Color.LightGray // ✅ Light grey when request sent or no trade
                            }
                        ),
                        enabled = !requestSent && book.status == "Available for Trade"
                    )

                }

                // Author
                Text(
                    text = "by ${book.author}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                book.description?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Rating
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (book.averageRating != null) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Purple80,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = book.averageRating.toString(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Text(
                            text = "No ratings",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }


            }
        }
    }
}

