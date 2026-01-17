package com.example.quotevaultapp.presentation.collections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
// TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
// import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
// import androidx.compose.material3.pulltorefresh.pulltorefresh
// import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
// import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
    viewModel: CollectionDetailViewModel = viewModel(),
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
    val availableQuotes by viewModel.availableQuotes.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoadingQuotes by viewModel.isLoadingQuotes.collectAsState()
    
    var showMenu by remember { mutableStateOf(false) }
    
    // TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
    // val pullToRefreshState = rememberPullToRefreshState()
    // 
    // // Sync pull-to-refresh state with ViewModel
    // LaunchedEffect(pullToRefreshState.isRefreshing) {
    //     if (pullToRefreshState.isRefreshing && !isRefreshing) {
    //         viewModel.refreshCollection()
    //     }
    //     if (!pullToRefreshState.isRefreshing && isRefreshing) {
    //         pullToRefreshState.endRefresh()
    //     }
    // }
    // 
    // LaunchedEffect(isRefreshing) {
    //     if (isRefreshing && !pullToRefreshState.isRefreshing) {
    //         pullToRefreshState.startRefresh()
    //     }
    // }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        val currentCollection = collection
                        Text(
                            text = currentCollection?.name ?: "Collection",
                            style = MaterialTheme.typography.titleLarge
                        )
                        val description = currentCollection?.description
                        if (description != null && description.isNotBlank()) {
                            Text(
                                text = description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditCollection() }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
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
                                    imageVector = Icons.Filled.Delete,
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
                            imageVector = Icons.Filled.MoreVert,
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
                    imageVector = Icons.Filled.Add,
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
                viewModel.isLoading && quotes.isEmpty() -> {
                    LoadingIndicator()
                }
                viewModel.error != null && quotes.isEmpty() -> {
                    ErrorState(
                        message = viewModel.error ?: "Failed to load collection",
                        onRetry = { viewModel.loadCollection(collectionId) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                quotes.isEmpty() && !viewModel.isLoading -> {
                    EmptyState(
                        icon = Icons.Filled.Add,
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
                            // .pulltorefresh(pullToRefreshState) // TEMPORARILY COMMENTED OUT
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
                        
                        // TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
                        // PullToRefreshContainer(
                        //     state = pullToRefreshState,
                        //     modifier = Modifier.align(Alignment.TopCenter)
                        // )
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
            availableQuotes = availableQuotes,
            searchQuery = searchQuery,
            isLoadingQuotes = isLoadingQuotes,
            onDismiss = { viewModel.hideAddQuoteDialog() },
            onSearchQueryChange = { viewModel.searchQuotes(it) },
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
                    imageVector = Icons.Filled.Delete,
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
                colors = ButtonDefaults.textButtonColors(
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
 * Add quote search dialog - allows searching and selecting quotes to add to collection
 */
@Composable
private fun AddQuoteSearchDialog(
    availableQuotes: List<Quote>,
    searchQuery: String,
    isLoadingQuotes: Boolean,
    onDismiss: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onQuoteSelected: (String) -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Quote to Collection") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Search field
                androidx.compose.material3.OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    label = { Text("Search quotes...") },
                    placeholder = { Text("Search by text or author") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            androidx.compose.material3.IconButton(
                                onClick = { onSearchQueryChange("") }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quotes list
                when {
                    isLoadingQuotes -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            LoadingIndicator()
                        }
                    }
                    availableQuotes.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isBlank()) "No quotes available" else "No quotes found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(
                                items = availableQuotes,
                                key = { it.id }
                            ) { quote ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable {
                                            onQuoteSelected(quote.id)
                                        }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp)
                                    ) {
                                        Text(
                                            text = quote.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "— ${quote.author}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}