package com.example.quotevaultapp.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pullrefresh.PullRefreshIndicator
import androidx.compose.material3.pullrefresh.pullRefresh
import androidx.compose.material3.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.presentation.components.EmptyState
import com.example.quotevaultapp.presentation.components.ErrorState
import com.example.quotevaultapp.presentation.components.QuoteCard
import com.example.quotevaultapp.presentation.components.QuoteCardShimmer
import com.example.quotevaultapp.presentation.components.QuoteOfTheDayShimmer
import com.example.quotevaultapp.presentation.theme.AppShapes
import com.example.quotevaultapp.presentation.theme.QuoteTypography
import kotlinx.coroutines.delay

/**
 * Home screen displaying quotes with category filtering, search, and pagination
 * 
 * @param viewModel HomeViewModel instance (injected via Hilt)
 * @param onQuoteClick Callback when a quote card is clicked
 * @param onFavoriteClick Callback when favorite button is clicked
 * @param onShareClick Callback when share button is clicked
 * @param fontSize User's preferred font size
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onQuoteClick: ((Quote) -> Unit)? = null,
    onFavoriteClick: (Quote) -> Unit = {},
    onShareClick: (Quote) -> Unit = {},
    fontSize: FontSize = FontSize.MEDIUM
) {
    val uiState by viewModel.uiState.collectAsState()
    val quotes by viewModel.quotes.collectAsState()
    val quoteOfTheDay by viewModel.quoteOfTheDay.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var isSearchActive by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh = { viewModel.onRefresh() }
    )
    
    // Handle pagination - load more when near the end
    LaunchedEffect(listState) {
        if (listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index?.let { 
            it >= listState.layoutInfo.totalItemsCount - 3 
        } == true && !uiState.isLoading && uiState.hasMore) {
            viewModel.loadMoreQuotes()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QuoteVault") },
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
                // Search Bar
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    onSearch = { viewModel.onSearch() },
                    active = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Search quotes or authors...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Search suggestions would go here
                }
                
                // Category Tabs (Horizontal Scroll)
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // All category
                    CategoryChip(
                        category = null,
                        label = "All",
                        isSelected = selectedCategory == null,
                        onClick = { viewModel.onCategorySelected(null) }
                    )
                    
                    // Other categories
                    QuoteCategory.values().filter { it != QuoteCategory.GENERAL }.forEach { category ->
                        CategoryChip(
                            category = category,
                            label = getCategoryDisplayName(category),
                            isSelected = selectedCategory == category,
                            onClick = { viewModel.onCategorySelected(category) }
                        )
                    }
                }
                
                // Main Content with Pull to Refresh
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pullRefresh(pullRefreshState)
                ) {
                    if (uiState.isLoading && quotes.isEmpty()) {
                    // Initial Loading - Show Shimmer
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            QuoteOfTheDayShimmer()
                        }
                        items(5) {
                            QuoteCardShimmer()
                        }
                    }
                } else if (uiState.error != null && quotes.isEmpty()) {
                    // Error State
                    ErrorState(
                        message = uiState.error ?: "Failed to load quotes",
                        onRetry = { viewModel.loadQuotes() },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (quotes.isEmpty() && !uiState.isLoading) {
                    // Empty State
                    EmptyState(
                        title = if (searchQuery.isNotBlank()) {
                            "No quotes found"
                        } else {
                            "No quotes available"
                        },
                        subtitle = if (searchQuery.isNotBlank()) {
                            "Try searching with different keywords"
                        } else {
                            "Check back later for new quotes"
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Content
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Quote of the Day (only show if no search query and first category selected)
                        if (searchQuery.isBlank() && selectedCategory == null && quoteOfTheDay != null) {
                            item {
                                AnimatedVisibility(
                                    visible = !uiState.isRefreshing,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    QuoteOfTheDayCard(
                                        quote = quoteOfTheDay!!,
                                        onFavoriteClick = { onFavoriteClick(quoteOfTheDay!!) },
                                        onShareClick = { onShareClick(quoteOfTheDay!!) },
                                        fontSize = fontSize
                                    )
                                }
                            }
                        }
                        
                        // Quotes List
                        items(
                            items = quotes,
                            key = { it.id }
                        ) { quote ->
                            AnimatedVisibility(
                                visible = !uiState.isRefreshing,
                                enter = fadeIn(),
                                exit = fadeOut()
                            ) {
                                QuoteCard(
                                    quote = quote,
                                    onFavoriteClick = onFavoriteClick,
                                    onShareClick = onShareClick,
                                    onClick = onQuoteClick,
                                    fontSize = fontSize
                                )
                            }
                        }
                        
                        // Loading More Indicator
                        if (uiState.isLoading && quotes.isNotEmpty()) {
                            item {
                                androidx.compose.foundation.layout.Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    QuoteCardShimmer()
                                }
                            }
                        }
                    }
                    
                    PullRefreshIndicator(
                        refreshing = uiState.isRefreshing,
                        state = pullRefreshState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

/**
 * Quote of the Day card component (larger, more prominent)
 */
@Composable
private fun QuoteOfTheDayCard(
    quote: Quote,
    onFavoriteClick: (Quote) -> Unit,
    onShareClick: (Quote) -> Unit,
    fontSize: FontSize
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Text(
                text = "Quote of the Day",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = quote.text,
                style = QuoteTypography.Large.copy(
                    fontSize = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "— ${quote.author}",
                    style = QuoteTypography.Author.copy(
                        fontSize = androidx.compose.ui.unit.TextUnit(16f, androidx.compose.ui.unit.TextUnitType.Sp)
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.IconButton(
                        onClick = { onFavoriteClick(quote) }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = if (quote.isFavorite) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = if (quote.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (quote.isFavorite) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                    androidx.compose.material3.IconButton(
                        onClick = { onShareClick(quote) }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share quote",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
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
