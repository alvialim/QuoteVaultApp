// TEMPORARILY COMMENTED OUT - Will be re-enabled once Glance widget setup is complete
// This file is disabled to allow the app to build while testing core features

/*
package com.example.quotevaultapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
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
import androidx.glance.unit.ColorProvider
import androidx.glance.unit.TextUnit
import androidx.glance.unit.TextUnitType
import androidx.glance.unit.dp
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking

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
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            QuoteWidgetEntryPoint::class.java
        )
        val quoteRepository = entryPoint.quoteRepository()
        
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
                .background(ColorProvider(0xFFFFFFFF)),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "QuoteVault",
                style = TextStyle(
                    color = ColorProvider(0xFF9E9E9E),
                    fontWeight = FontWeight.Bold
                ),
                modifier = GlanceModifier.padding(bottom = 8.dp)
            )
            
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = quote?.text ?: "Loading quote...",
                style = TextStyle(
                    color = ColorProvider(0xFF212121),
                    fontSize = TextUnit(16f, TextUnitType.Sp)
                ),
                modifier = GlanceModifier.fillMaxWidth()
            )
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = quote?.let { "— ${it.author}" } ?: "",
                    style = TextStyle(
                        color = ColorProvider(0xFF757575),
                        fontSize = TextUnit(14f, TextUnitType.Sp),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

class QuoteGlanceWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuoteGlanceWidget()
}
*/
