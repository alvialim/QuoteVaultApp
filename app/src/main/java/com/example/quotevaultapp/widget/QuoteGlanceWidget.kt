package com.example.quotevaultapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.Color
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

/**
 * Jetpack Glance widget for displaying quote of the day
 * Modern widget implementation using Compose-like API
 */
class QuoteGlanceWidget : GlanceAppWidget() {
    
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface QuoteWidgetEntryPoint {
        fun quoteRepository(): QuoteRepository
    }
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }
    
    @Composable
    private fun WidgetContent(context: Context) {
        // Get repository via Hilt EntryPoint
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuoteWidgetEntryPoint::class.java
        )
        val quoteRepository = entryPoint.quoteRepository()
        
        // Fetch quote (blocking for widget update)
        val quote = runBlocking {
            when (val result = quoteRepository.getQuoteOfTheDay()) {
                is Result.Success -> result.data
                is Result.Error -> null
            }
        }
        
        WidgetContent(quote = quote)
    }
    
    @Composable
    fun WidgetContent(quote: Quote?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(16.dp)
                .background(Color.White),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // App Branding
            Text(
                text = "QuoteVault",
                style = TextStyle(
                    color = Color(0xFF9E9E9E),
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            // Quote Text
            Text(
                text = quote?.text ?: "Loading quote...",
                style = TextStyle(
                    color = Color(0xFF212121),
                    fontSize = 16.dp
                ),
                textAlign = TextAlign.Start,
                modifier = GlanceModifier.fillMaxWidth()
            )
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            // Author Text
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = quote?.let { "— ${it.author}" } ?: "",
                    style = TextStyle(
                        color = Color(0xFF757575),
                        fontSize = 14.dp,
                        fontWeight = FontWeight.Normal
                    ),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

/**
 * Glance Widget Receiver
 */
class QuoteGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteGlanceWidget()
}
