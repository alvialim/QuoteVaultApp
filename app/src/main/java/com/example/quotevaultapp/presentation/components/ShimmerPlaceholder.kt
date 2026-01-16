package com.example.quotevaultapp.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.quotevaultapp.presentation.theme.AppShapes

/**
 * Shimmer effect modifier for loading placeholders
 */
@Composable
fun shimmerBrush(): Brush {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        ),
        start = Offset(shimmerTranslateAnim - 300f, shimmerTranslateAnim - 300f),
        end = Offset(shimmerTranslateAnim, shimmerTranslateAnim)
    )
}

/**
 * Shimmer placeholder for quote card
 */
@Composable
fun QuoteCardShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.quoteCard,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Category badge shimmer
            Box(
                modifier = Modifier
                    .width(80.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(shimmerBrush())
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Quote text shimmer (3 lines)
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 2) 0.7f else 1f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush())
                )
                if (it < 2) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Author shimmer
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shimmerBrush())
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(shimmerBrush())
                )
            }
        }
    }
}

/**
 * Shimmer placeholder for quote of the day card
 */
@Composable
fun QuoteOfTheDayShimmer(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.quoteCardElevated,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Label shimmer
            Box(
                modifier = Modifier
                    .width(140.dp)
                    .height(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quote text shimmer (4 lines)
            repeat(4) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (it == 3) 0.6f else 1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shimmerBrush())
                )
                if (it < 3) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Author shimmer
            Box(
                modifier = Modifier
                    .width(150.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(shimmerBrush())
            )
        }
    }
}
