package com.example.quotevaultapp.presentation.collections

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quotevaultapp.domain.model.Collection
import com.example.quotevaultapp.presentation.components.EmptyState
import com.example.quotevaultapp.presentation.components.ErrorState
import com.example.quotevaultapp.presentation.components.LoadingIndicator
import com.example.quotevaultapp.presentation.theme.AppShapes

/**
 * Collections screen displaying user's quote collections in a grid layout
 * 
 * @param viewModel CollectionsViewModel instance (injected via Hilt)
 * @param onCollectionClick Callback when a collection is tapped
 * @param onCreateCollection Callback when FAB is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    viewModel: CollectionsViewModel = viewModel(),
    onCollectionClick: (Collection) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val collections by viewModel.collections.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    
    // Load collections when screen first appears
    LaunchedEffect(Unit) {
        viewModel.loadCollections()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateDialog() },
                shape = AppShapes.fab,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create collection"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val isLoading = viewModel.uiState.value is CollectionsUiState.Loading
            val error = (viewModel.uiState.value as? CollectionsUiState.Error)?.message
            
            when {
                isLoading && collections.isEmpty() -> {
                    LoadingIndicator()
                }
                error != null && collections.isEmpty() -> {
                    ErrorState(
                        message = error,
                        onRetry = { viewModel.loadCollections() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                collections.isEmpty() -> {
                    EmptyState(
                        icon = Icons.Filled.Folder,
                        title = "No collections yet",
                        subtitle = "Create your first collection to organize your favorite quotes",
                        actionButtonText = "Create Collection",
                        onActionClick = { viewModel.showCreateDialog() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                else -> {
                    // Stitch Design: Enhanced grid spacing
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 160.dp),
                        contentPadding = PaddingValues(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(
                            items = collections,
                            key = { it.id }
                        ) { collection ->
                            CollectionCard(
                                collection = collection,
                                onClick = { onCollectionClick(collection) }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Create Collection Dialog
    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { viewModel.hideCreateDialog() },
            onSave = { name, description ->
                viewModel.createCollection(name, description)
            }
        )
    }
}

/**
 * Create collection dialog
 */
@Composable
private fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Collection") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                androidx.compose.material3.OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., Morning Motivation, Work Quotes") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                androidx.compose.material3.OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("Add a description for this collection") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(
                onClick = {
                    onSave(name.trim(), description.takeIf { it.isNotBlank() })
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Collection card component for grid display
 */
@Composable
private fun CollectionCard(
    collection: Collection,
    onClick: () -> Unit
) {
    // Stitch Design: Enhanced collection card elevation and shadows
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = AppShapes.quoteCard,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp,
            focusedElevation = 6.dp,
            disabledElevation = 0.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        // Stitch Design: Improved spacing in collection card
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Collection Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Folder,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Collection Name
            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // Description (if available)
            if (collection.description != null && collection.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = collection.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Quote Count
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${collection.quoteIds.size}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (collection.quoteIds.size == 1) "quote" else "quotes",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
