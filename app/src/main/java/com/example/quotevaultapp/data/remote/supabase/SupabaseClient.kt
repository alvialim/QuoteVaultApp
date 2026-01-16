package com.example.quotevaultapp.data.remote.supabase

import com.example.quotevaultapp.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.realtime

/**
 * Singleton Supabase client instance
 * Provides centralized access to Supabase services (Auth, Postgrest, Realtime)
 */
object SupabaseClient {
    
    /**
     * Lazy initialization of the Supabase client
     * Configured with Auth, Postgrest, and Realtime modules
     */
    val client: SupabaseClient by lazy {
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
    
    /**
     * Get the Auth module instance
     */
    val auth = client.auth
    
    /**
     * Get the Postgrest module instance for database queries
     */
    val postgrest = client.postgrest
    
    /**
     * Get the Realtime module instance for real-time subscriptions
     */
    val realtime = client.realtime
}
