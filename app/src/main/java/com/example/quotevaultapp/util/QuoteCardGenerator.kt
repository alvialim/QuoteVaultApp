package com.example.quotevaultapp.util

import android.content.Context
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.presentation.components.QuoteCardTemplateStyle
import com.example.quotevaultapp.presentation.theme.AppShapes
import com.example.quotevaultapp.presentation.theme.QuoteTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Generate bitmap from quote card composable for sharing
 */
object QuoteCardGenerator {
    
    /**
     * Generate bitmap from quote card with selected template
     * 
     * @param context Application context
     * @param quote Quote to display
     * @param style Template style to use
     * @param width Width of the bitmap in pixels (default: 1080 for high quality)
     * @param height Height of the bitmap in pixels (default: 1920 for high quality)
     * @return Bitmap of the quote card
     */
    suspend fun generateBitmap(
        context: Context,
        quote: Quote,
        style: QuoteCardTemplateStyle,
        width: Int = 1080,
        height: Int = 1920
    ): android.graphics.Bitmap = withContext(Dispatchers.Main) {
        // Create ComposeView to render composable
        val composeView = ComposeView(context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    QuoteCardWithWatermark(
                        quote = quote,
                        style = style,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                    )
                }
            }
        }
        
        // Measure and layout
        val specWidth = android.view.View.MeasureSpec.makeMeasureSpec(width, android.view.View.MeasureSpec.EXACTLY)
        val specHeight = android.view.View.MeasureSpec.makeMeasureSpec(height, android.view.View.MeasureSpec.EXACTLY)
        composeView.measure(specWidth, specHeight)
        composeView.layout(0, 0, width, height)
        
        // Wait for composition to complete
        delay(100)
        
        // Render to bitmap
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        composeView.draw(canvas)
        
        bitmap
    }
    
    /**
     * Quote card composable with app watermark
     */
    @Composable
    private fun QuoteCardWithWatermark(
        quote: Quote,
        style: QuoteCardTemplateStyle,
        modifier: Modifier = Modifier
    ) {
        Box(modifier = modifier) {
            // Quote card template
            when (style) {
                QuoteCardTemplateStyle.GRADIENT -> GradientQuoteCardWithWatermark(quote, modifier)
                QuoteCardTemplateStyle.MINIMAL -> MinimalQuoteCardWithWatermark(quote, modifier)
                QuoteCardTemplateStyle.BOLD -> BoldQuoteCardWithWatermark(quote, modifier)
            }
            
            // App watermark (bottom right corner)
            Text(
                text = "QuoteVault",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }
    }
    
    /**
     * Gradient quote card with watermark
     */
    @Composable
    private fun GradientQuoteCardWithWatermark(
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
                .fillMaxSize()
                .background(gradient)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = """"${quote.text}"""",
                    style = QuoteTypography.Large.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "— ${quote.author}",
                        style = QuoteTypography.Author.copy(
                            fontSize = 20.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
    
    /**
     * Minimal quote card with watermark
     */
    @Composable
    private fun MinimalQuoteCardWithWatermark(
        quote: Quote,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
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
                Text(
                    text = """“""",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 80.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Light
                    ),
                    modifier = Modifier.align(Alignment.Start)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = quote.text,
                    style = QuoteTypography.Medium.copy(fontSize = 26.sp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = """— ${quote.author}""",
                        style = QuoteTypography.Author.copy(fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = """”""",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 80.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        fontWeight = FontWeight.Light
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
    
    /**
     * Bold quote card with watermark
     */
    @Composable
    private fun BoldQuoteCardWithWatermark(
        quote: Quote,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
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
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
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
                
                Text(
                    text = quote.author.uppercase(),
                    style = QuoteTypography.Author.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
