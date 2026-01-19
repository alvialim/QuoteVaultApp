package com.example.quotevaultapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.quotevaultapp.MainActivity
import com.example.quotevaultapp.R
import com.example.quotevaultapp.domain.model.Result
import com.example.quotevaultapp.domain.repository.QuoteRepository
import com.example.quotevaultapp.data.remote.supabase.SupabaseQuoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * App Widget Provider for displaying quote of the day on home screen
 */
class QuoteWidget : AppWidgetProvider() {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all instances of the widget
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle manual refresh action if needed
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, QuoteWidget::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }
    
    /**
     * Update widget with quote of the day
     */
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.quote_widget)
        
        // Set pending intent to open app when widget is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("from_widget", true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
        
        // Create repository instance directly (without Hilt)
        val quoteRepository: QuoteRepository = SupabaseQuoteRepository()
        
        // Fetch quote of the day
        scope.launch {
            try {
                val result = quoteRepository.getQuoteOfTheDay()
                
                when (result) {
                    is Result.Success -> {
                        updateWidgetViews(views, result.data)
                    }
                    is Result.Error -> {
                        views.setTextViewText(
                            R.id.widget_quote_text,
                            "Failed to load quote"
                        )
                        views.setTextViewText(R.id.widget_quote_author, "")
                    }
                    is Result.Loading -> {
                        // Loading state - keep current widget state
                    }
                is Result.Loading -> {
                    // Loading state
                }
                }
                
                // Update widget on main thread
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                // Handle error - show placeholder text
                views.setTextViewText(R.id.widget_quote_text, "Loading quote...")
                views.setTextViewText(R.id.widget_quote_author, "")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
    
    /**
     * Update RemoteViews with quote data
     */
    private fun updateWidgetViews(views: RemoteViews, quote: com.example.quotevaultapp.domain.model.Quote) {
        views.setTextViewText(R.id.widget_quote_text, quote.text)
        views.setTextViewText(R.id.widget_quote_author, "— ${quote.author}")
    }
    
    companion object {
        private const val ACTION_REFRESH_WIDGET = "com.example.quotevaultapp.ACTION_REFRESH_WIDGET"
        
        /**
         * Manually refresh widget (can be called from app)
         */
        fun refreshWidget(context: Context) {
            val intent = Intent(context, QuoteWidget::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
