package com.example.quotevaultapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.util.QuoteCardGenerator
import com.example.quotevaultapp.util.ShareHelper
import com.example.quotevaultapp.util.rememberStoragePermission
import kotlinx.coroutines.launch

/**
 * Bottom sheet for sharing quotes with multiple options
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareBottomSheet(
    quote: Quote,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    var isGeneratingBitmap by remember { mutableStateOf(false) }
    var isSavingToGallery by remember { mutableStateOf<QuoteCardTemplateStyle?>(null) }
    var pendingSaveStyle by remember { mutableStateOf<QuoteCardTemplateStyle?>(null) }
    
    // Permission helper for saving to gallery
    val requestStoragePermissionLauncher = rememberStoragePermission(
        onGranted = {
            // Permission granted, proceed with save
            pendingSaveStyle?.let { selectedStyle ->
                scope.launch {
                    try {
                        val result = ShareHelper.saveQuoteCardToGallery(
                            context = context,
                            quote = quote,
                            style = selectedStyle
                        )
                        when (result) {
                            is com.example.quotevaultapp.domain.model.Result.Success -> {
                                android.util.Log.d("ShareBottomSheet", "Saved to gallery: ${result.data}")
                            }
                            is com.example.quotevaultapp.domain.model.Result.Error -> {
                                android.util.Log.e("ShareBottomSheet", "Error saving to gallery: ${result.exception.message}", result.exception)
                            }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("ShareBottomSheet", "Error saving to gallery: ${e.message}", e)
                    } finally {
                        isSavingToGallery = null
                        pendingSaveStyle = null
                    }
                }
            }
        },
        onDenied = {
            android.util.Log.w("ShareBottomSheet", "Storage permission denied")
            isSavingToGallery = null
            pendingSaveStyle = null
        }
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Share as Text Section
            Text(
                text = "Share Quote",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            ListItem(
                headlineContent = { Text("Share as Text") },
                leadingContent = {
                    Icon(
                        Icons.Default.TextFields,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        ShareHelper.shareQuoteAsText(context, quote)
                        onDismiss()
                    }
                    .padding(vertical = 4.dp)
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Share as Image Section
            Text(
                text = "Share as Image",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            if (isGeneratingBitmap) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(QuoteCardTemplateStyle.values()) { style ->
                        CardStylePreview(
                            quote = quote,
                            style = style,
                            onShareClick = {
                                scope.launch {
                                    isGeneratingBitmap = true
                                    try {
                                        val bitmap = QuoteCardGenerator.generateBitmap(
                                            context = context,
                                            quote = quote,
                                            style = style,
                                            width = 1080,
                                            height = 1920
                                        )
                                        ShareHelper.shareQuoteAsImage(context, bitmap)
                                        onDismiss()
                                    } catch (e: Exception) {
                                        android.util.Log.e("ShareBottomSheet", "Error sharing quote card: ${e.message}", e)
                                    } finally {
                                        isGeneratingBitmap = false
                                    }
                                }
                            },
                            onSaveClick = { selectedStyle ->
                                // Request permission first, then save
                                isSavingToGallery = selectedStyle
                                pendingSaveStyle = selectedStyle
                                requestStoragePermissionLauncher()
                            },
                            isSaving = isSavingToGallery == style
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Preview card for style selection with share/save actions
 */
@Composable
private fun CardStylePreview(
    quote: Quote,
    style: QuoteCardTemplateStyle,
    onShareClick: () -> Unit,
    onSaveClick: (QuoteCardTemplateStyle) -> Unit,
    isSaving: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Small preview of card (100x180dp scaled down)
        Box(
            modifier = Modifier
                .size(100.dp, 180.dp)
                .clip(MaterialTheme.shapes.medium)
        ) {
            QuoteCardTemplate(
                quote = quote,
                style = style,
                modifier = Modifier
                    .size(100.dp, 180.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = style.name.replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = onShareClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Share,
                    contentDescription = "Share",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            IconButton(
                onClick = { onSaveClick(style) },
                modifier = Modifier.size(40.dp),
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.Download,
                        contentDescription = "Save to Gallery",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

