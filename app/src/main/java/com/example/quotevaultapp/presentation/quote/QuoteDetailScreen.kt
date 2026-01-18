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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.presentation.components.QuoteCardTemplate
import com.example.quotevaultapp.presentation.components.QuoteCardTemplateStyle
import com.example.quotevaultapp.util.QuoteCardGenerator
import com.example.quotevaultapp.util.ShareHelper
import com.example.quotevaultapp.util.rememberStoragePermission
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
    quoteId: String,
    quote: Quote? = null,
    onBack: () -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM,
    viewModel: QuoteDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Helper function to get Activity context
    val activityContext: Context = remember(context) {
        var ctx: Context = context
        while (ctx is android.content.ContextWrapper) {
            ctx = ctx.baseContext
            if (ctx is android.app.Activity) {
                return@remember ctx
            }
        }
        context
    }
    var selectedTemplate by remember { mutableStateOf(QuoteCardTemplateStyle.GRADIENT) }
    var isGeneratingBitmap by remember { mutableStateOf(false) }
    var isSavingImage by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Use provided quote or load from ViewModel
    val quoteState by viewModel.quote.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val displayedQuote = quote ?: quoteState
    
    android.util.Log.d("QuoteDetail", "Quote detail state: quoteId=$quoteId, displayedQuote=${displayedQuote?.id}, isLoading=$isLoading, error=$error")
    
    // Permission helper for saving to gallery
    // This will request permission at runtime if not granted, then save the image
    val requestStoragePermission = rememberStoragePermission(
        onGranted = {
            // Step 2: Permission granted (or already granted), proceed with save
            android.util.Log.d("QuoteDetail", "Step 2: Storage permission granted, starting save operation")
            scope.launch {
                // Get current quote state at the time of save (not captured value)
                val currentQuote = quote ?: viewModel.quote.value
                android.util.Log.d("QuoteDetail", "Current quote for save: ${currentQuote?.id}")
                
                if (currentQuote == null) {
                    android.util.Log.w("QuoteDetail", "Cannot save: quote is null when permission granted")
                    Toast.makeText(
                        context,
                        "Cannot save: Quote not loaded. Please wait for the quote to load.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }
                
                isSavingImage = true
                try {
                    android.util.Log.d("QuoteDetail", "Generating bitmap for save with template: $selectedTemplate")
                    // Use Activity context for bitmap generation
                    val activityCtx = activityContext ?: context
                    android.util.Log.d("QuoteDetail", "Using context for save: ${activityCtx.javaClass.simpleName}")
                    val result = ShareHelper.saveQuoteCardToGallery(
                        context = activityCtx,
                        quote = currentQuote,
                        style = selectedTemplate
                    )
                    when (result) {
                        is com.example.quotevaultapp.domain.model.Result.Success -> {
                            android.util.Log.d("QuoteDetail", "Image saved to gallery: ${result.data}")
                            // Show toast message on success
                            Toast.makeText(
                                context,
                                "Image saved to Gallery!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        is com.example.quotevaultapp.domain.model.Result.Error -> {
                            android.util.Log.e("QuoteDetail", "Failed to save image: ${result.exception.message}", result.exception)
                            Toast.makeText(
                                context,
                                "Failed to save: ${result.exception.message ?: "Unknown error"}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("QuoteDetail", "Failed to save image: ${e.message}", e)
                    e.printStackTrace()
                    Toast.makeText(
                        context,
                        "Failed to save: ${e.message ?: "Unknown error"}",
                        Toast.LENGTH_LONG
                    ).show()
                } finally {
                    isSavingImage = false
                }
            }
        },
        onDenied = {
            android.util.Log.w("QuoteDetail", "Storage permission denied by user, cannot save image")
            Toast.makeText(
                context,
                "Storage permission is required to save images. Please grant permission in Settings.",
                Toast.LENGTH_LONG
            ).show()
        }
    )
    
    // Load quote if not provided
    LaunchedEffect(quoteId) {
        android.util.Log.d("QuoteDetail", "LaunchedEffect triggered: quoteId=$quoteId, provided quote=${quote?.id}")
        if (quote != null) {
            android.util.Log.d("QuoteDetail", "Setting provided quote: ${quote.id}")
            viewModel.setQuote(quote)
        } else if (quoteState == null && !isLoading) {
            android.util.Log.d("QuoteDetail", "Loading quote from repository: quoteId=$quoteId")
            viewModel.loadQuote(quoteId)
        } else {
            android.util.Log.d("QuoteDetail", "Skipping load: quoteState=${quoteState?.id}, isLoading=$isLoading")
        }
    }
    
    // Show loading or error if quote is not available
    if (displayedQuote == null && isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            com.example.quotevaultapp.presentation.components.LoadingIndicator()
        }
        return
    }
    
    // Show error if loading failed
    if (displayedQuote == null && error != null && !isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                com.example.quotevaultapp.presentation.components.ErrorState(
                    message = error ?: "Failed to load quote",
                    onRetry = {
                        viewModel.loadQuote(quoteId)
                    }
                )
            }
        }
        return
    }
    
    // Show loading if quote still null after a moment
    if (displayedQuote == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            com.example.quotevaultapp.presentation.components.LoadingIndicator()
        }
        return
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        onClick = { displayedQuote?.let { ShareHelper.shareQuoteAsText(context, it) } }
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
                displayedQuote?.let { q ->
                    QuoteCardTemplate(
                        quote = q,
                        style = selectedTemplate,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                        android.util.Log.d("QuoteDetail", "Share Image button clicked")
                        if (displayedQuote == null) {
                            android.util.Log.w("QuoteDetail", "Cannot share: quote is null")
                            return@Button
                        }
                        isGeneratingBitmap = true
                        scope.launch {
                            try {
                                android.util.Log.d("QuoteDetail", "Generating bitmap with template: $selectedTemplate")
                                val bitmap = QuoteCardGenerator.generateBitmap(
                                    context = context,
                                    quote = displayedQuote!!,
                                    style = selectedTemplate
                                )
                                android.util.Log.d("QuoteDetail", "Bitmap generated successfully, size: ${bitmap.width}x${bitmap.height}")
                                ShareHelper.shareQuoteAsImage(activityContext, bitmap)
                                android.util.Log.d("QuoteDetail", "Share intent started")
                            } catch (e: Exception) {
                                android.util.Log.e("QuoteDetail", "Failed to share image: ${e.message}", e)
                                e.printStackTrace()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Failed to share image: ${e.message ?: "Unknown error"}")
                                }
                            } finally {
                                isGeneratingBitmap = false
                            }
                        }
                    },
                    enabled = !isGeneratingBitmap && displayedQuote != null,
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
                        android.util.Log.d("QuoteDetail", "Save button clicked. Quote state: displayedQuote=${displayedQuote?.id}, isLoading=$isLoading")
                        
                        // Check if quote is available before requesting permission
                        val currentQuote = displayedQuote
                        if (currentQuote == null) {
                            android.util.Log.w("QuoteDetail", "Cannot save: quote is null when button clicked")
                            if (isLoading) {
                                Toast.makeText(
                                    context,
                                    "Please wait for the quote to load",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Cannot save: Quote not available. ${error ?: "Please try again."}",
                                    Toast.LENGTH_LONG
                                ).show()
                                // Try to reload the quote
                                if (quoteId.isNotEmpty()) {
                                    viewModel.loadQuote(quoteId)
                                }
                            }
                            return@OutlinedButton
                        }
                        
                        // Step 1: Request permission at runtime first
                        // This will show system permission dialog if not granted
                        // Step 2: Save will happen automatically after permission is granted
                        android.util.Log.d("QuoteDetail", "Step 1: Requesting storage permission at runtime...")
                        requestStoragePermission()
                    },
                    enabled = !isSavingImage && displayedQuote != null,
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
