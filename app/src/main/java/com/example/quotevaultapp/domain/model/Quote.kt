package com.example.quotevaultapp.domain.model

/**
 * Domain model representing a quote in the application
 */
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: QuoteCategory,
    val createdAt: Long,
    val isFavorite: Boolean = false
)
