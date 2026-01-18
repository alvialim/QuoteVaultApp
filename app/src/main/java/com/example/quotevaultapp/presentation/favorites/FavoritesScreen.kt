package com.example.quotevaultapp.presentation.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
// TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
// import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
// import androidx.compose.material3.pulltorefresh.pulltorefresh
// import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.Scaffold
// import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.presentation.components.EmptyState
import com.example.quotevaultapp.presentation.components.ErrorState
import com.example.quotevaultapp.presentation.components.QuoteCard
import com.example.quotevaultapp.presentation.components.QuoteCardShimmer
import com.example.quotevaultapp.util.ShareHelper
import kotlin.math.roundToInt

/**
 * Favorites screen displaying user's favorite quotes
 * 
 * @param viewModel FavoritesViewModel instance (injected via Hilt)
 * @param onQuoteClick Callback when a quote card is clicked
 * @param onShareClick Callback when share button is clicked
 * @param fontSize User's preferred font size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = viewModel(),
    onQuoteClick: ((Quote) -> Unit)? = null,
    onShareClick: (Quote) -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM
) {
    val context = LocalContext.current
    
    val uiState by viewModel.uiState.collectAsState()
    val favorites by viewModel.favorites.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    // Load favorites when screen appears (cloud sync)
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }
    
    val listState = rememberLazyListState()
    
    // Reload favorites when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }
    
    // TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
    // val pullToRefreshState = rememberPullToRefreshState()
    // 
    // // Sync pull-to-refresh state with ViewModel
    // LaunchedEffect(pullToRefreshState.isRefreshing) {
    //     if (pullToRefreshState.isRefreshing && !isRefreshing) {
    //         viewModel.refreshFavorites()
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
    
    // Filter favorites based on search and category
    val filteredFavorites = favorites.filter { quote ->
        val matchesCategory = selectedCategory == null || quote.category == selectedCategory
        val matchesSearch = searchQuery.isBlank() || 
            quote.text.contains(searchQuery, ignoreCase = true) ||
            quote.author.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Favorites")
                        // Sync indicator
                        if (isSyncing) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = "Syncing",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Search Field (Simple TextField - doesn't expand to full screen)
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search favorites...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { 
                                    viewModel.onSearchQueryChange("")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors()
                )
                
                // Category Tabs (Horizontal Scroll)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All category
                    item {
                        CategoryChip(
                            category = null,
                            label = "All",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.onCategorySelected(null) }
                        )
                    }
                    
                    // Other categories
                    items(
                        items = QuoteCategory.values().filter { it != QuoteCategory.GENERAL },
                        key = { it.name }
                    ) { category ->
                        CategoryChip(
                            category = category,
                            label = getCategoryDisplayName(category),
                            isSelected = selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
                
                // Main Content (Pull to Refresh temporarily disabled)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // .pulltorefresh(pullToRefreshState) // TEMPORARILY COMMENTED OUT
                ) {
                    // Determine current state
                    val isInitialLoading = viewModel.isLoading && favorites.isEmpty()
                    val isError = viewModel.error != null && favorites.isEmpty()
                    val isEmpty = filteredFavorites.isEmpty() && !viewModel.isLoading && favorites.isEmpty()
                    val isSearchEmpty = filteredFavorites.isEmpty() && !viewModel.isLoading && favorites.isNotEmpty() && searchQuery.isNotBlank()
                    val isCategoryEmpty = filteredFavorites.isEmpty() && !viewModel.isLoading && favorites.isNotEmpty() && selectedCategory != null && searchQuery.isBlank()
                    val hasFilteredResults = filteredFavorites.isNotEmpty()
                    
                    when {
                        // Initial Loading State - Show Shimmer
                        isInitialLoading -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(5) {
                                    QuoteCardShimmer()
                                }
                            }
                        }
                        
                        // Error State - Show Error Message with Retry
                        isError -> {
                            ErrorState(
                                message = viewModel.error ?: "Failed to load favorites",
                                onRetry = { viewModel.loadFavorites() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Empty State - No Favorites at All
                        isEmpty -> {
                            EmptyState(
                                icon = Icons.Default.Favorite,
                                title = "No favorites yet",
                                subtitle = "Start favoriting quotes to see them here",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Empty State - Search Results
                        isSearchEmpty -> {
                            EmptyState(
                                icon = Icons.Default.Favorite,
                                title = "No matching favorites",
                                subtitle = "Try searching with different keywords",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Empty State - Category Filter
                        isCategoryEmpty -> {
                            EmptyState(
                                icon = Icons.Default.Favorite,
                                title = "No favorites in this category",
                                subtitle = "Try selecting a different category or add quotes to this category",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Content State - Show Favorites
                        hasFilteredResults -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                items(
                                    items = filteredFavorites,
                                    key = { it.id }
                                ) { quote ->
                                    SwipeableQuoteCard(
                                        quote = quote,
                                        onDelete = { viewModel.removeFromFavorites(quote.id) },
                                        onFavoriteClick = { viewModel.toggleFavorite(quote.id) },
                                        onShareClick = { quote -> ShareHelper.shareQuoteAsText(context, quote) },
                                        onClick = onQuoteClick,
                                        fontSize = fontSize
                                    )
                                }
                            }
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

/**
 * Swipeable quote card with delete action
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
    val animatedOffset by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = tween(durationMillis = 200),
        label = "swipe_offset"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Delete action background
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
        
        // Quote Card
        QuoteCard(
            quote = quote,
            onFavoriteClick = { onFavoriteClick(quote.id) },
            onShareClick = onShareClick,
            onClick = onClick,
            fontSize = fontSize,
            modifier = Modifier
                .offset {
                    IntOffset(
                        animatedOffset.roundToInt(),
                        0
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            with(density) {
                                if (offsetX < -swipeThreshold.toPx()) {
                                    // Swipe threshold reached, delete
                                    onDelete()
                                    offsetX = 0f
                                } else {
                                    // Reset position
                                    offsetX = 0f
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        // Only allow left swipe (negative drag)
                        offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                    }
                }
        )
    }
}

/**
 * Category chip component
 */
@Composable
private fun CategoryChip(
    category: QuoteCategory?,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    InputChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(label) },
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

/**
 * Get display name for category
 */
private fun getCategoryDisplayName(category: QuoteCategory): String = when (category) {
    QuoteCategory.MOTIVATION -> "Motivation"
    QuoteCategory.LOVE -> "Love"
    QuoteCategory.SUCCESS -> "Success"
    QuoteCategory.WISDOM -> "Wisdom"
    QuoteCategory.HUMOR -> "Humor"
    QuoteCategory.GENERAL -> "General"
}
