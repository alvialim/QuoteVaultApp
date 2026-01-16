package com.example.quotevaultapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.presentation.theme.AppShapes
import com.example.quotevaultapp.presentation.theme.QuoteTypography
import com.example.quotevaultapp.presentation.theme.quoteTextStyle
import com.example.quotevaultapp.domain.model.FontSize

/**
 * Reusable quote card component with favorite/share actions and category badge
 * 
 * @param quote The quote to display
 * @param onFavoriteClick Callback when favorite button is clicked
 * @param onShareClick Callback when share button is clicked
 * @param onClick Callback when the card itself is clicked (optional)
 * @param fontSize User's preferred font size for quote text
 * @param modifier Modifier for the card
 */
@Composable
fun QuoteCard(
    quote: Quote,
    onFavoriteClick: (Quote) -> Unit,
    onShareClick: (Quote) -> Unit,
    onClick: ((Quote) -> Unit)? = null,
    fontSize: FontSize = FontSize.MEDIUM,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        onClick = { onClick(quote) }
                    )
                } else {
                    Modifier
                }
            ),
        shape = AppShapes.quoteCard,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category Badge
            CategoryBadge(
                category = quote.category,
                modifier = Modifier.align(Alignment.Start)
            )
            
            // Quote Text
            Text(
                text = quote.text,
                style = quoteTextStyle(fontSize),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Quote: ${quote.text}"
                    }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Author
            Text(
                text = "— ${quote.author}",
                style = QuoteTypography.Author,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Author: ${quote.author}"
                    }
            )
            
            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Favorite Button
                IconButton(
                    onClick = { onFavoriteClick(quote) },
                    modifier = Modifier.semantics {
                        contentDescription = if (quote.isFavorite) {
                            "Remove from favorites, ${quote.author}"
                        } else {
                            "Add to favorites, ${quote.author}"
                        }
                    }
                ) {
                    Icon(
                        imageVector = if (quote.isFavorite) {
                            Icons.Filled.Favorite
                        } else {
                            Icons.Filled.FavoriteBorder
                        },
                        contentDescription = null, // Handled by parent semantics
                        tint = if (quote.isFavorite) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Share Button
                IconButton(
                    onClick = { onShareClick(quote) },
                    modifier = Modifier.semantics {
                        contentDescription = "Share quote by ${quote.author}"
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = null, // Handled by parent semantics
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/**
 * Category badge component
 */
@Composable
private fun CategoryBadge(
    category: QuoteCategory,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(category)
    val categoryName = getCategoryDisplayName(category)
    
    Surface(
        modifier = modifier
            .clip(AppShapes.categoryChip),
        color = categoryColor.copy(alpha = 0.15f),
        shape = AppShapes.categoryChip
    ) {
        Text(
            text = categoryName,
            style = QuoteTypography.Category,
            color = categoryColor,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .semantics {
                    contentDescription = "Category: $categoryName"
                }
        )
    }
}

/**
 * Get display color for category
 */
@Composable
private fun getCategoryColor(category: QuoteCategory) = when (category) {
    QuoteCategory.MOTIVATION -> androidx.compose.ui.graphics.Color(0xFFFF6B35) // Orange
    QuoteCategory.LOVE -> androidx.compose.ui.graphics.Color(0xFFE91E63) // Pink
    QuoteCategory.SUCCESS -> androidx.compose.ui.graphics.Color(0xFF4CAF50) // Green
    QuoteCategory.WISDOM -> MaterialTheme.colorScheme.primary // Purple
    QuoteCategory.HUMOR -> androidx.compose.ui.graphics.Color(0xFFFFC107) // Amber
    QuoteCategory.GENERAL -> MaterialTheme.colorScheme.secondary // Blue
}

/**
 * Get display name for category
 */
private fun getCategoryDisplayName(category: QuoteCategory) = when (category) {
    QuoteCategory.MOTIVATION -> "Motivation"
    QuoteCategory.LOVE -> "Love"
    QuoteCategory.SUCCESS -> "Success"
    QuoteCategory.WISDOM -> "Wisdom"
    QuoteCategory.HUMOR -> "Humor"
    QuoteCategory.GENERAL -> "General"
}
