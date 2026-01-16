package com.example.quotevaultapp.domain.repository

import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations
 * Defines the contract for user authentication, registration, and profile management
 */
interface AuthRepository {
    
    /**
     * Sign up a new user with email and password
     * @param email User's email address
     * @param password User's password
     * @return Result containing the created User on success
     */
    suspend fun signUp(email: String, password: String): Result<User>
    
    /**
     * Sign in an existing user with email and password
     * @param email User's email address
     * @param password User's password
     * @return Result containing the authenticated User on success
     */
    suspend fun signIn(email: String, password: String): Result<User>
    
    /**
     * Sign out the current user
     * @return Result<Unit> indicating success or failure
     */
    suspend fun signOut(): Result<Unit>
    
    /**
     * Get the currently authenticated user
     * @return Result containing the current User if authenticated, null otherwise
     */
    suspend fun getCurrentUser(): Result<User?>
    
    /**
     * Send a password reset email to the user
     * @param email User's email address
     * @return Result<Unit> indicating success or failure
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>
    
    /**
     * Update user profile information
     * @param displayName New display name (can be null to remove)
     * @param avatarUrl New avatar URL (can be null to remove)
     * @return Result containing the updated User on success
     */
    suspend fun updateProfile(displayName: String?, avatarUrl: String?): Result<User>
    
    /**
     * Observe authentication state changes
     * Emits the current User when authenticated, null when signed out
     * @return Flow that emits User? based on authentication state
     */
    fun observeAuthState(): Flow<User?>
}
