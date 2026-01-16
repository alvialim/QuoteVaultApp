package com.example.quotevaultapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.presentation.theme.AppShapes
import com.example.quotevaultapp.presentation.theme.QuoteTypography

/**
 * Template style for quote cards when sharing
 */
enum class QuoteCardTemplateStyle {
    GRADIENT,
    MINIMAL,
    BOLD
}

/**
 * Quote card template for sharing with different visual styles
 * 
 * @param quote The quote to display
 * @param style Template style to use
 * @param modifier Modifier for the container
 */
@Composable
fun QuoteCardTemplate(
    quote: Quote,
    style: QuoteCardTemplateStyle = QuoteCardTemplateStyle.GRADIENT,
    modifier: Modifier = Modifier
) {
    when (style) {
        QuoteCardTemplateStyle.GRADIENT -> GradientQuoteCard(quote, modifier)
        QuoteCardTemplateStyle.MINIMAL -> MinimalQuoteCard(quote, modifier)
        QuoteCardTemplateStyle.BOLD -> BoldQuoteCard(quote, modifier)
    }
}

/**
 * Style 1: Gradient background quote card
 */
@Composable
private fun GradientQuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        ),
        start = Offset(0f, 0f),
        end = Offset(1000f, 1000f)
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(gradient)
            .padding(32.dp)
            .semantics {
                contentDescription = "Quote by ${quote.author}: ${quote.text}"
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quote Text
            Text(
                text = """"${quote.text}"""",
                style = QuoteTypography.Large.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                ),
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Quote: ${quote.text}"
                    }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Author
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "— ${quote.author}",
                    style = QuoteTypography.Author.copy(
                        fontSize = 18.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    ),
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .semantics {
                            contentDescription = "Author: ${quote.author}"
                        }
                )
            }
        }
    }
}

/**
 * Style 2: Minimal quote card with quote marks
 */
@Composable
private fun MinimalQuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .semantics {
                contentDescription = "Quote by ${quote.author}: ${quote.text}"
            },
        color = MaterialTheme.colorScheme.surface,
        shape = AppShapes.quoteCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Opening Quote Mark
            Text(
                text = """“""",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.Start)
                    .semantics { contentDescription = "Opening quote mark" }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quote Text
            Text(
                text = quote.text,
                style = QuoteTypography.Medium.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Quote: ${quote.text}"
                    }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Closing Quote Mark and Author
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = """— ${quote.author}""",
                    style = QuoteTypography.Author.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .semantics {
                            contentDescription = "Author: ${quote.author}"
                        }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Closing Quote Mark
            Text(
                text = """”""",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 72.sp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    fontWeight = FontWeight.Light
                ),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                modifier = Modifier
                    .align(Alignment.End)
                    .semantics { contentDescription = "Closing quote mark" }
            )
        }
    }
}

/**
 * Style 3: Bold typography quote card
 */
@Composable
private fun BoldQuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
            .semantics {
                contentDescription = "Quote by ${quote.author}: ${quote.text}"
            },
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = AppShapes.quoteCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quote Text - Bold and Large
            Text(
                text = quote.text,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Quote: ${quote.text}"
                    }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Divider
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Author - Bold
            Text(
                text = quote.author.uppercase(),
                style = QuoteTypography.Author.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Author: ${quote.author}"
                    }
            )
        }
    }
}
