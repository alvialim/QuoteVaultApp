package com.example.quotevaultapp.presentation.quote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.presentation.components.QuoteCardTemplate
import com.example.quotevaultapp.presentation.components.QuoteCardTemplateStyle
import com.example.quotevaultapp.util.QuoteCardGenerator
import com.example.quotevaultapp.util.ShareHelper
import kotlinx.coroutines.launch

/**
 * Quote detail screen for viewing and sharing quotes
 * 
 * @param quote Quote to display
 * @param onBack Callback to navigate back
 * @param fontSize User's preferred font size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quote: Quote,
    onBack: () -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM,
    viewModel: QuoteDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTemplate by remember { mutableStateOf(QuoteCardTemplateStyle.GRADIENT) }
    var isGeneratingBitmap by remember { mutableStateOf(false) }
    var isSavingImage by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quote Detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { ShareHelper.shareQuoteAsText(context, quote) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share as text"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Template Selector
            Text(
                text = "Choose Template",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(QuoteCardTemplateStyle.values().toList()) { style ->
                    TemplateChip(
                        style = style,
                        isSelected = selectedTemplate == style,
                        onClick = { selectedTemplate = style }
                    )
                }
            }
            
            // Quote Card Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp),
                contentAlignment = Alignment.Center
            ) {
                QuoteCardTemplate(
                    quote = quote,
                    style = selectedTemplate,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Share as Image Button
                Button(
                    onClick = {
                        isGeneratingBitmap = true
                        scope.launch {
                            try {
                                val bitmap = QuoteCardGenerator.generateBitmap(
                                    context = context,
                                    quote = quote,
                                    style = selectedTemplate
                                )
                                ShareHelper.shareQuoteAsImage(context, bitmap)
                            } catch (e: Exception) {
                                android.util.Log.e("QuoteDetail", "Failed to share image: ${e.message}", e)
                            } finally {
                                isGeneratingBitmap = false
                            }
                        }
                    },
                    enabled = !isGeneratingBitmap,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isGeneratingBitmap) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Share Image")
                }
                
                // Save to Gallery Button
                OutlinedButton(
                    onClick = {
                        isSavingImage = true
                        scope.launch {
                            try {
                                val bitmap = QuoteCardGenerator.generateBitmap(
                                    context = context,
                                    quote = quote,
                                    style = selectedTemplate
                                )
                                val uri = ShareHelper.saveImageToGallery(context, bitmap)
                                if (uri != null) {
                                    // Show success message (could use a snackbar)
                                    android.util.Log.d("QuoteDetail", "Image saved: $uri")
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("QuoteDetail", "Failed to save image: ${e.message}", e)
                            } finally {
                                isSavingImage = false
                            }
                        }
                    },
                    enabled = !isSavingImage,
                    modifier = Modifier.weight(1f)
                ) {
                    if (isSavingImage) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Save")
                }
            }
        }
    }
}

/**
 * Template selection chip
 */
@Composable
private fun TemplateChip(
    style: QuoteCardTemplateStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Text(
            text = when (style) {
                QuoteCardTemplateStyle.GRADIENT -> "Gradient"
                QuoteCardTemplateStyle.MINIMAL -> "Minimal"
                QuoteCardTemplateStyle.BOLD -> "Bold"
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}
