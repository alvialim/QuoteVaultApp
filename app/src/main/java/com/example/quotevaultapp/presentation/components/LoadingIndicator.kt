package com.example.quotevaultapp.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Loading indicator component with branding
 * 
 * @param modifier Modifier for the container
 * @param contentDescription Accessibility description (default: "Loading")
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    contentDescription: String = "Loading"
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .semantics {
                this.contentDescription = contentDescription
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 4.dp
        )
    }
}

/**
 * Small inline loading indicator
 */
@Composable
fun SmallLoadingIndicator(
    modifier: Modifier = Modifier,
    contentDescription: String = "Loading"
) {
    CircularProgressIndicator(
        modifier = modifier
            .size(24.dp)
            .semantics {
                this.contentDescription = contentDescription
            },
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp
    )
}
