package com.example.quotevaultapp.data.remote.supabase

import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.model.User
import com.example.quotevaultapp.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime
import com.example.quotevaultapp.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

/**
 * Supabase implementation of AuthRepository
 * Handles authentication operations using Supabase Auth SDK
 */
class SupabaseAuthRepository : AuthRepository {
    
    private val supabaseClient: SupabaseClient by lazy {
        require(BuildConfig.SUPABASE_URL.isNotEmpty()) {
            "SUPABASE_URL must be configured in local.properties"
        }
        require(BuildConfig.SUPABASE_ANON_KEY.isNotEmpty()) {
            "SUPABASE_ANON_KEY must be configured in local.properties"
        }
        
        createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(io.github.jan.supabase.gotrue.Auth)
            install(io.github.jan.supabase.postgrest.Postgrest)
            install(io.github.jan.supabase.realtime.Realtime)
        }
    }
    
    private val authStateFlow = MutableStateFlow<User?>(null)
    
    init {
        // Initialize auth state
        loadCurrentUser()
    }
    
    override suspend fun signUp(email: String, password: String): Result<User> {
        return try {
            val result = supabaseClient.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = result.user
            if (user != null) {
                val domainUser = mapToDomainUser(user)
                authStateFlow.value = domainUser
                Result.Success(domainUser)
            } else {
                Result.Error(Exception("User creation failed: No user returned"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun signIn(email: String, password: String): Result<User> {
        return try {
            val result = supabaseClient.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            
            val user = result.user
            if (user != null) {
                val domainUser = loadUserProfile(user.id)
                authStateFlow.value = domainUser
                Result.Success(domainUser)
            } else {
                Result.Error(Exception("Sign in failed: No user returned"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            supabaseClient.auth.signOut()
            authStateFlow.value = null
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User?> {
        return try {
            val currentSession = supabaseClient.auth.currentSessionOrNull()
            if (currentSession != null && currentSession.user != null) {
                val user = loadUserProfile(currentSession.user.id)
                authStateFlow.value = user
                Result.Success(user)
            } else {
                authStateFlow.value = null
                Result.Success(null)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun sendPasswordReset(email: String): Result<Unit> {
        return try {
            supabaseClient.auth.resetPasswordForEmail(email)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun updateProfile(displayName: String?, avatarUrl: String?): Result<User> {
        return try {
            val currentUser = supabaseClient.auth.currentUserOrNull()
                ?: return Result.Error(Exception("No authenticated user"))
            
            // Update profile in Supabase profiles table
            supabaseClient.postgrest.from("profiles")
                .update(mapOf(
                    "display_name" to displayName,
                    "avatar_url" to avatarUrl
                )) {
                    filter {
                        eq("id", currentUser.id)
                    }
                }
            
            // Reload user profile
            val updatedUser = loadUserProfile(currentUser.id)
            authStateFlow.value = updatedUser
            Result.Success(updatedUser)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override fun observeAuthState(): Flow<User?> {
        return authStateFlow.asStateFlow()
    }
    
    /**
     * Load the current user from auth state
     */
    private suspend fun loadCurrentUser() {
        try {
            val currentSession = supabaseClient.auth.currentSessionOrNull()
            if (currentSession != null && currentSession.user != null) {
                val user = loadUserProfile(currentSession.user.id)
                authStateFlow.value = user
            } else {
                authStateFlow.value = null
            }
        } catch (e: Exception) {
            authStateFlow.value = null
        }
    }
    
    /**
     * Load user profile from profiles table and combine with auth user data
     */
    private suspend fun loadUserProfile(userId: String): User {
        return try {
            // Fetch profile from profiles table
            val profile = supabaseClient.postgrest.from("profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeSingle<ProfileData>()
            
            // Get auth user for email
            val authUser = supabaseClient.auth.currentUserOrNull()
            
            User(
                id = userId,
                email = authUser?.email ?: "",
                displayName = profile.display_name,
                avatarUrl = profile.avatar_url,
                theme = AppTheme.valueOf(profile.theme.uppercase()),
                fontSize = FontSize.valueOf(profile.font_size.uppercase())
            )
        } catch (e: Exception) {
            // If profile doesn't exist, create default user from auth data
            val authUser = supabaseClient.auth.currentUserOrNull()
                ?: throw Exception("User not found")
            
            User(
                id = userId,
                email = authUser.email ?: "",
                displayName = null,
                avatarUrl = null,
                theme = AppTheme.SYSTEM,
                fontSize = FontSize.MEDIUM
            )
        }
    }
    
    /**
     * Map Supabase auth user to domain User
     */
    private fun mapToDomainUser(authUser: io.github.jan.supabase.gotrue.user.UserInfo): User {
        return User(
            id = authUser.id,
            email = authUser.email ?: "",
            displayName = authUser.userMetadata?.get("display_name") as? String,
            avatarUrl = authUser.userMetadata?.get("avatar_url") as? String,
            theme = AppTheme.SYSTEM,
            fontSize = FontSize.MEDIUM
        )
    }
    
    /**
     * Data class for profile data from Supabase
     */
    @Serializable
    private data class ProfileData(
        val id: String,
        val display_name: String?,
        val avatar_url: String?,
        val theme: String,
        val font_size: String
    )
}
