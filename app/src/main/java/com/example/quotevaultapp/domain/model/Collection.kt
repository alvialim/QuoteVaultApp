package com.example.quotevaultapp.domain.model

/**
 * Domain model representing a user-created collection of quotes
 */
data class Collection(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val quoteIds: List<String> = emptyList(),
    val createdAt: Long
)
