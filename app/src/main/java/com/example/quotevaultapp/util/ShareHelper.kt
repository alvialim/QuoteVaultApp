package com.example.quotevaultapp.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.quotevaultapp.domain.model.Quote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Helper class for sharing quotes as text or images
 */
object ShareHelper {
    
    /**
     * Share quote as plain text
     * 
     * @param context Activity context
     * @param quote Quote to share
     */
    fun shareQuoteAsText(context: Context, quote: Quote) {
        val shareText = buildString {
            append("\"")
            append(quote.text)
            append("\"")
            append("\n\n")
            append("— ")
            append(quote.author)
            append("\n\n")
            append("Shared via QuoteVault")
        }
        
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        
        context.startActivity(Intent.createChooser(intent, "Share Quote"))
    }
    
    /**
     * Share quote card as image
     * 
     * @param context Context (should be Activity context for startActivity)
     * @param bitmap Bitmap of the quote card
     */
    fun shareQuoteAsImage(context: Context, bitmap: Bitmap) {
        try {
            android.util.Log.d("ShareHelper", "Starting share image process")
            val uri = saveImageToCache(context, bitmap)
            if (uri == null) {
                android.util.Log.e("ShareHelper", "Failed to save image to cache")
                return
            }
            android.util.Log.d("ShareHelper", "Image saved to cache, URI: $uri")
            
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooserIntent = Intent.createChooser(intent, "Share Quote Card").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Try to get Activity context if needed
            val activityContext: Context = run {
                if (context is android.app.Activity) {
                    context
                } else {
                    var ctx: Context? = context
                    var activity: android.app.Activity? = null
                    while (ctx is android.content.ContextWrapper) {
                        ctx = ctx.baseContext
                        if (ctx is android.app.Activity) {
                            activity = ctx
                            break
                        }
                    }
                    activity ?: context
                }
            }
            
            android.util.Log.d("ShareHelper", "Starting activity with context: ${activityContext.javaClass.simpleName}")
            activityContext.startActivity(chooserIntent)
            android.util.Log.d("ShareHelper", "Share intent started successfully")
        } catch (e: android.content.ActivityNotFoundException) {
            android.util.Log.e("ShareHelper", "No activity found to handle share intent: ${e.message}", e)
        } catch (e: Exception) {
            android.util.Log.e("ShareHelper", "Failed to share image: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    /**
     * Share quote card as image
     * 
     * @param context Activity context
     * @param quote Quote to share
     * @param style Card style to use
     */
    suspend fun shareQuoteCard(
        context: Context,
        quote: Quote,
        style: com.example.quotevaultapp.presentation.components.QuoteCardTemplateStyle
    ) {
        withContext(Dispatchers.Main) {
            val bitmap = QuoteCardGenerator.generateBitmap(
                context = context,
                quote = quote,
                style = style,
                width = 1080,
                height = 1920
            )
            shareQuoteAsImage(context, bitmap)
        }
    }
    
    /**
     * Save quote card image to device gallery
     * 
     * @param context Application context
     * @param quote Quote to save
     * @param style Card style to use
     * @return Result with file path or error
     */
    suspend fun saveQuoteCardToGallery(
        context: Context,
        quote: Quote,
        style: com.example.quotevaultapp.presentation.components.QuoteCardTemplateStyle
    ): com.example.quotevaultapp.domain.model.Result<String> {
        return try {
            android.util.Log.d("ShareHelper", "Starting save quote card to gallery")
            // Ensure we have Activity context for bitmap generation
            val activityContext: Context = run {
                if (context is android.app.Activity) {
                    context
                } else {
                    var ctx: Context? = context
                    var activity: android.app.Activity? = null
                    while (ctx is android.content.ContextWrapper) {
                        ctx = ctx.baseContext
                        if (ctx is android.app.Activity) {
                            activity = ctx
                            break
                        }
                    }
                    activity ?: context
                }
            }
            android.util.Log.d("ShareHelper", "Using context for save: ${activityContext.javaClass.simpleName}")
            
            // Generate bitmap first
            android.util.Log.d("ShareHelper", "Generating bitmap for save")
            val bitmap = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                QuoteCardGenerator.generateBitmap(
                    context = activityContext,
                    quote = quote,
                    style = style,
                    width = 1080,
                    height = 1920
                )
            }
            android.util.Log.d("ShareHelper", "Bitmap generated successfully: ${bitmap.width}x${bitmap.height}")
            
            // Save to gallery
            android.util.Log.d("ShareHelper", "Saving bitmap to gallery")
            val uri = saveImageToGalleryInternal(context, bitmap)
            if (uri != null) {
                android.util.Log.d("ShareHelper", "Image saved successfully to: $uri")
                com.example.quotevaultapp.domain.model.Result.Success(uri.toString())
            } else {
                android.util.Log.e("ShareHelper", "Failed to save image to gallery: URI is null")
                com.example.quotevaultapp.domain.model.Result.Error(Exception("Failed to save image to gallery: URI is null"))
            }
        } catch (e: Exception) {
            android.util.Log.e("ShareHelper", "Failed to save quote card to gallery: ${e.message}", e)
            e.printStackTrace()
            com.example.quotevaultapp.domain.model.Result.Error(e)
        }
    }
    
    /**
     * Internal method to save bitmap to gallery
     */
    private suspend fun saveImageToGalleryInternal(
        context: Context,
        bitmap: Bitmap
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, generateImageFileName())
                put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QuoteVault")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return@withContext null
            
            // Save bitmap to URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)) {
                    android.util.Log.e("ShareHelper", "Failed to compress bitmap")
                    return@withContext null
                }
                outputStream.flush()
                android.util.Log.d("ShareHelper", "Bitmap compressed and saved to URI: $uri")
            } ?: run {
                android.util.Log.e("ShareHelper", "Failed to open output stream for URI: $uri")
                return@withContext null
            }
            
            // Mark as not pending (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            
            // Notify media scanner (Android < 10)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
                context.sendBroadcast(intent)
            }
            
            uri
        } catch (e: Exception) {
            android.util.Log.e("ShareHelper", "Failed to save image to gallery: ${e.message}", e)
            null
        }
    }
    
    /**
     * Save image to cache directory for sharing
     * Returns FileProvider URI for sharing
     */
    private fun saveImageToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cacheDir = File(context.cacheDir, "shared_images")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            
            val imageFile = File(cacheDir, "quote_${System.currentTimeMillis()}.png")
            
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            android.util.Log.e("ShareHelper", "Failed to save image to cache: ${e.message}", e)
            null
        }
    }
    
    /**
     * Generate unique filename for saved images
     */
    private fun generateImageFileName(): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return "QuoteVault_$timestamp.png"
    }
}
