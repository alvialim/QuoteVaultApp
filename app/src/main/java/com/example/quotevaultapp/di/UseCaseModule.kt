package com.example.quotevaultapp.di

import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.domain.usecase.GetQuotesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing use case instances
 * Use cases coordinate between repositories to perform business operations
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    /**
     * Provides GetQuotesUseCase instance
     * TODO: Update this when GetQuotesUseCase is implemented with proper constructor
     */
    @Provides
    @Singleton
    fun provideGetQuotesUseCase(
        quoteRepository: QuoteRepository
    ): GetQuotesUseCase {
        // TODO: Update this once GetQuotesUseCase has a constructor
        // For now, this is a placeholder
        return GetQuotesUseCase()
    }
}
