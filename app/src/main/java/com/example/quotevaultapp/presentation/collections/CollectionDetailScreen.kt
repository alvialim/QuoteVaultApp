package com.example.quotevaultapp.presentation.collections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.presentation.components.EmptyState
import com.example.quotevaultapp.presentation.components.ErrorState
import com.example.quotevaultapp.presentation.components.LoadingIndicator
import com.example.quotevaultapp.presentation.components.QuoteCard
import com.example.quotevaultapp.presentation.components.QuoteCardShimmer
import kotlin.math.roundToInt

/**
 * Collection detail screen showing quotes in a collection
 * 
 * @param collectionId ID of the collection to display
 * @param viewModel CollectionDetailViewModel instance (injected via Hilt)
 * @param onQuoteClick Callback when a quote is clicked
 * @param onShareClick Callback when share button is clicked
 * @param onBack Callback to navigate back
 * @param fontSize User's preferred font size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    collection: com.example.quotevaultapp.domain.model.Collection? = null,
    viewModel: CollectionDetailViewModel = hiltViewModel(),
    onQuoteClick: ((Quote) -> Unit)? = null,
    onShareClick: (Quote) -> Unit = {},
    onBack: () -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM
) {
    // Set collection if provided, then load quotes
    LaunchedEffect(collectionId, collection) {
        if (collection != null) {
            viewModel.setCollection(collection)
        }
        viewModel.loadCollection(collectionId)
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val collection by viewModel.collection.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showAddQuoteDialog by viewModel.showAddQuoteDialog.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshCollection() }
    )
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = collection?.name ?: "Collection",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (collection?.description != null && collection.description.isNotBlank()) {
                            Text(
                                text = collection.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditCollection() }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit collection"
                        )
                    }
                    androidx.compose.material3.DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        androidx.compose.material3.DropdownMenuItem(
                            text = { Text("Delete Collection") },
                            onClick = {
                                showMenu = false
                                viewModel.showDeleteCollection()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddQuoteSearch() },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add quote to collection"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && quotes.isEmpty() -> {
                    LoadingIndicator()
                }
                uiState.error != null && quotes.isEmpty() -> {
                    ErrorState(
                        message = uiState.error ?: "Failed to load collection",
                        onRetry = { viewModel.loadCollection(collectionId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                quotes.isEmpty() && !uiState.isLoading -> {
                    EmptyState(
                        icon = Icons.Default.Add,
                        title = "No quotes in collection",
                        subtitle = "Add quotes to get started",
                        actionButtonText = "Add Quote",
                        onActionClick = { viewModel.showAddQuoteSearch() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullRefresh(pullRefreshState)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = quotes,
                                key = { it.id }
                            ) { quote ->
                                SwipeableQuoteCard(
                                    quote = quote,
                                    onDelete = { viewModel.removeQuote(quote.id) },
                                    onFavoriteClick = { /* Handle favorite in collection detail */ },
                                    onShareClick = { onShareClick(quote) },
                                    onClick = onQuoteClick,
                                    fontSize = fontSize
                                )
                            }
                        }
                        
                        PullRefreshIndicator(
                            refreshing = isRefreshing,
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
        }
    }
    
    // Edit Collection Dialog
    if (showEditDialog && collection != null) {
        EditCollectionDialog(
            collection = collection!!,
            onDismiss = { viewModel.hideEditDialog() },
            onSave = { name, description ->
                viewModel.updateCollection(name, description)
            }
        )
    }
    
    // Delete Collection Dialog
    if (showDeleteDialog) {
        DeleteCollectionDialog(
            collectionName = collection?.name ?: "collection",
            onDismiss = { viewModel.hideDeleteDialog() },
            onConfirm = {
                viewModel.deleteCollection()
                onBack()
            }
        )
    }
    
    // Add Quote Search Dialog
    if (showAddQuoteDialog) {
        AddQuoteSearchDialog(
            collectionId = collectionId,
            onDismiss = { viewModel.hideAddQuoteDialog() },
            onQuoteSelected = { quoteId ->
                viewModel.addQuote(quoteId)
            }
        )
    }
}

/**
 * Swipeable quote card for collection detail (same as favorites)
 */
@Composable
private fun SwipeableQuoteCard(
    quote: Quote,
    onDelete: () -> Unit,
    onFavoriteClick: (String) -> Unit,
    onShareClick: (Quote) -> Unit,
    onClick: ((Quote) -> Unit)?,
    fontSize: FontSize
) {
    val swipeThreshold = 150.dp
    val density = LocalDensity.current
    
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by androidx.compose.animation.core.animateFloatAsState(
        targetValue = offsetX,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
        label = "swipe_offset"
    )
    
    Box(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = offsetX < 0,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        QuoteCard(
            quote = quote,
            onFavoriteClick = { onFavoriteClick(quote.id) },
            onShareClick = onShareClick,
            onClick = onClick,
            fontSize = fontSize,
            modifier = Modifier
                .offset {
                    IntOffset(animatedOffset.roundToInt(), 0)
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            with(density) {
                                if (offsetX < -swipeThreshold.toPx()) {
                                    onDelete()
                                    offsetX = 0f
                                } else {
                                    offsetX = 0f
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                    }
                }
        )
    }
}

/**
 * Edit collection dialog
 */
@Composable
private fun EditCollectionDialog(
    collection: com.example.quotevaultapp.domain.model.Collection,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf(collection.name) }
    var description by remember { mutableStateOf(collection.description ?: "") }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Collection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(name.trim(), description.takeIf { it.isNotBlank() })
                    onDismiss()
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Delete collection confirmation dialog
 */
@Composable
private fun DeleteCollectionDialog(
    collectionName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Collection") },
        text = { Text("Are you sure you want to delete \"$collectionName\"? This action cannot be undone.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = androidx.compose.material3.TextButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Add quote search dialog (placeholder - would need full search implementation)
 */
@Composable
private fun AddQuoteSearchDialog(
    collectionId: String,
    onDismiss: () -> Unit,
    onQuoteSelected: (String) -> Unit
) {
    // This would be a full search dialog implementation
    // For now, just a placeholder
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Quote") },
        text = { Text("Search and select quotes to add to collection") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
