package com.example.quotevaultapp.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
// TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
// import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
// import androidx.compose.material3.pulltorefresh.pulltorefresh
// import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
// import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.quotevaultapp.presentation.components.MainBottomNavigationBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.material3.Surface
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.presentation.home.UiState
import com.example.quotevaultapp.presentation.home.QuoteOfTheDayErrorCard
import com.example.quotevaultapp.presentation.components.EmptyState
import com.example.quotevaultapp.presentation.components.ErrorState
import com.example.quotevaultapp.presentation.components.QuoteCard
import com.example.quotevaultapp.presentation.components.QuoteCardShimmer
import com.example.quotevaultapp.presentation.components.QuoteOfTheDayShimmer
import com.example.quotevaultapp.presentation.theme.AppShapes
import com.example.quotevaultapp.util.ShareHelper
import com.example.quotevaultapp.presentation.theme.QuoteTypography

/**
 * Home screen displaying quotes with category filtering, search, and pagination
 * 
 * @param viewModel HomeViewModel instance (injected via Hilt)
 * @param onQuoteClick Callback when a quote card is clicked
 * @param onFavoriteClick Callback when favorite button is clicked
 * @param onShareClick Callback when share button is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    viewModel: HomeViewModel = viewModel(),
    onQuoteClick: ((Quote) -> Unit)? = null,
    onFavoriteClick: (Quote) -> Unit = { viewModel.toggleFavorite(it.id) },
    onShareClick: (Quote) -> Unit = {}
) {
    val context = LocalContext.current
    // Get font size from CompositionLocal (provided in MainActivity)
    val fontSize = com.example.quotevaultapp.presentation.theme.LocalFontSize.current
    
    val uiState by viewModel.uiState.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val quoteOfTheDay by viewModel.quoteOfTheDay.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    
    // Derived state from ViewModel computed properties
    val isLoading = viewModel.isLoading
    val hasMore = viewModel.hasMore
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val error = viewModel.error
    
    val listState = rememberLazyListState()
    
    // TEMPORARILY COMMENTED OUT - Pull to refresh will be re-enabled later
    // val pullToRefreshState = rememberPullToRefreshState()
    // 
    // // Sync pull-to-refresh state with ViewModel
    // LaunchedEffect(pullToRefreshState.isRefreshing) {
    //     if (pullToRefreshState.isRefreshing && !isRefreshing) {
    //         viewModel.onRefresh()
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
    
    // Handle pagination - load more when near the end
    // Observe uiState and quotes to trigger recomposition when state changes
    LaunchedEffect(listState, uiState, quotes.size) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            val totalItemsCount = layoutInfo.totalItemsCount
            val currentIsLoading = viewModel.isLoading
            val currentHasMore = viewModel.hasMore
            PaginationState(lastVisibleItemIndex, totalItemsCount, currentIsLoading, currentHasMore)
        }
            .distinctUntilChanged()
            .filter { state ->
                state.totalCount > 0 && state.lastIndex >= 0
            }
            .collect { state ->
                // Load more when we're near the end (within 3 items of the last item)
                if (state.lastIndex >= state.totalCount - 3 && !state.isLoading && state.hasMore) {
                    android.util.Log.d("HomeScreen", "Loading more quotes. Last index: ${state.lastIndex}, Total: ${state.totalCount}")
                    viewModel.loadMoreQuotes()
                }
            }
    }
    
    val currentRoute = navController?.currentBackStackEntryAsState()?.value?.destination?.route
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QuoteVault") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            navController?.let {
                MainBottomNavigationBar(
                    navController = it,
                    currentRoute = currentRoute
                )
            }
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
                // Stitch Design: Increased spacing and padding
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    placeholder = { Text("Search quotes or authors...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
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
                // Stitch Design: Improved spacing between chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                
                // Quote of the Day Section (Prominent at Top)
                // Only show when not searching and no category selected
                if (searchQuery.isBlank() && selectedCategory == null) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(
                            animationSpec = tween(400)
                        ) + slideInVertically(
                            initialOffsetY = { -it },
                            animationSpec = tween(400)
                        ),
                        exit = fadeOut(
                            animationSpec = tween(200)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            when (val qotdState = quoteOfTheDay) {
                                is UiState.Loading -> {
                                    QuoteOfTheDayShimmer()
                                }
                                is UiState.Success -> {
                                        QuoteOfTheDayCard(
                                        quote = qotdState.data,
                                        onFavoriteClick = { onFavoriteClick(qotdState.data) },
                                        onShareClick = { quote -> ShareHelper.shareQuoteAsText(context, quote) },
                                        onClick = onQuoteClick
                                    )
                                }
                                is UiState.Error -> {
                                    QuoteOfTheDayErrorCard(
                                        message = qotdState.message,
                                        onRetry = { viewModel.refreshQuoteOfTheDay() }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Main Content (Pull to Refresh temporarily disabled)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // .pulltorefresh(pullToRefreshState) // TEMPORARILY COMMENTED OUT
                ) {
                    // Determine current state
                    val isInitialLoading = uiState is HomeUiState.Loading && quotes.isEmpty() && searchQuery.isBlank()
                    val isError = uiState is HomeUiState.Error && quotes.isEmpty()
                    val isEmpty = quotes.isEmpty() && !isLoading && searchQuery.isBlank() && selectedCategory == null
                    val isSearchEmpty = quotes.isEmpty() && !isLoading && searchQuery.isNotBlank()
                    val isCategoryEmpty = quotes.isEmpty() && !isLoading && selectedCategory != null && searchQuery.isBlank()
                    
                    when {
                        // Initial Loading State - Show Shimmer
                        isInitialLoading -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                items(5) {
                                    QuoteCardShimmer()
                                }
                            }
                        }
                        
                        // Error State - Show Error Message with Retry
                        isError -> {
                            ErrorState(
                                message = error ?: "Failed to load quotes",
                                onRetry = {
                                    if (searchQuery.isNotBlank()) {
                                        viewModel.onSearch()
                                    } else if (selectedCategory != null) {
                                        viewModel.onCategorySelected(selectedCategory)
                                    } else {
                                        viewModel.loadQuotes()
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Empty State - Search Results
                        isSearchEmpty -> {
                            EmptyState(
                                title = "No quotes found",
                                subtitle = "Try searching with different keywords or authors",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Empty State - Category Filter
                        isCategoryEmpty -> {
                            EmptyState(
                                title = "No quotes in this category",
                                subtitle = "Try selecting a different category",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Empty State - General
                        isEmpty -> {
                            EmptyState(
                                title = "No quotes available",
                                subtitle = "Check back later for new quotes",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        
                        // Content State - Show Quotes
                        else -> {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Quotes List
                                items(
                                    items = quotes,
                                    key = { it.id }
                                ) { quote ->
                                    QuoteCard(
                                        quote = quote,
                                        onFavoriteClick = onFavoriteClick,
                                        onShareClick = { quote -> ShareHelper.shareQuoteAsText(context, quote) },
                                        onClick = onQuoteClick
                                    )
                                }
                                
                                // Loading More Indicator (Pagination)
                                if (isLoadingMore && quotes.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 24.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(48.dp),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    strokeWidth = 3.dp
                                                )
                                                Text(
                                                    text = "Loading more quotes...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
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
 * Quote of the Day card component (larger, more prominent with gradient)
 */
@Composable
private fun QuoteOfTheDayCard(
    quote: Quote,
    onFavoriteClick: (Quote) -> Unit,
    onShareClick: (Quote) -> Unit,
    onClick: ((Quote) -> Unit)? = null
) {
    // Get font size from CompositionLocal
    val fontSize = com.example.quotevaultapp.presentation.theme.LocalFontSize.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        onClick = { onClick(quote) },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                } else {
                    Modifier
                }
            ),
        shape = AppShapes.quoteCardElevated,
        // Stitch Design: Enhanced elevation for prominence (QOTD card)
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 16.dp,
            hoveredElevation = 14.dp,
            focusedElevation = 14.dp,
            disabledElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        // Stitch Design: More visible border for QOTD card
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            ) {
                // Badge/Chip for "Quote of the Day"
                // Stitch Design: Enhanced badge styling
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    shadowElevation = 2.dp,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "✨ Quote of the Day",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Quote text (larger font based on fontSize preference)
                val quoteTextSize = when (fontSize) {
                    FontSize.SMALL -> 18.sp
                    FontSize.MEDIUM -> 20.sp
                    FontSize.LARGE -> 22.sp
                }
                
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = quoteTextSize,
                        lineHeight = androidx.compose.ui.unit.TextUnit(28f, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Start,
                    fontWeight = FontWeight.Normal
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Author and action buttons
                // Stitch Design: Better spacing between author and actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "— ${quote.author}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { onFavoriteClick(quote) },
                            modifier = Modifier.semantics {
                                contentDescription = if (quote.isFavorite) "Remove from favorites" else "Add to favorites"
                            }
                        ) {
                            Icon(
                                imageVector = if (quote.isFavorite) {
                                    Icons.Filled.Favorite
                                } else {
                                    Icons.Filled.FavoriteBorder
                                },
                                contentDescription = null,
                                tint = if (quote.isFavorite) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        IconButton(
                            onClick = { onShareClick(quote) },
                            modifier = Modifier.semantics {
                                contentDescription = "Share quote"
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category chip component - Stitch Design enhanced
 * Better elevation, spacing, and visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
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
        label = { 
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            ) 
        },
        // Stitch Design: Enhanced chip colors and styling
        colors = InputChipDefaults.inputChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        // Stitch Design: Better border styling
        border = InputChipDefaults.inputChipBorder(
            enabled = true,
            selected = isSelected,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            borderColor = MaterialTheme.colorScheme.outline,
            selectedBorderWidth = 2.dp,
            borderWidth = 1.dp
        )
    )
}

/**
 * Data class for pagination state in snapshotFlow
 */
private data class PaginationState(
    val lastIndex: Int,
    val totalCount: Int,
    val isLoading: Boolean,
    val hasMore: Boolean
)

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
