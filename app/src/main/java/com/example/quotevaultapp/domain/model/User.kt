package com.example.quotevaultapp.domain.model

/**
 * Domain model representing a user in the application
 */
data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val theme: AppTheme = AppTheme.SYSTEM,
    val fontSize: FontSize = FontSize.MEDIUM
)
