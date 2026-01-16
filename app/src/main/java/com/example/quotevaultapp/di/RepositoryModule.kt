package com.example.quotevaultapp.di

import com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseCollectionRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import com.example.quotevaultapp.domain.repository.AuthRepository
import com.example.quotevaultapp.domain.repository.CollectionRepository
import com.example.quotevaultapp.domain.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository interfaces to implementations
 * Uses @Binds for cleaner, more efficient code generation
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * Binds AuthRepository interface to SupabaseAuthRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        supabaseAuthRepository: SupabaseAuthRepository
    ): AuthRepository
    
    /**
     * Binds QuoteRepository interface to SupabaseQuoteRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindQuoteRepository(
        supabaseQuoteRepository: SupabaseQuoteRepository
    ): QuoteRepository
    
    /**
     * Binds CollectionRepository interface to SupabaseCollectionRepository implementation
     */
    @Binds
    @Singleton
    abstract fun bindCollectionRepository(
        supabaseCollectionRepository: SupabaseCollectionRepository
    ): CollectionRepository
}
