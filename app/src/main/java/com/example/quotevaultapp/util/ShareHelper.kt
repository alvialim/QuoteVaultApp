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
     * @param context Activity context
     * @param bitmap Bitmap of the quote card
     */
    fun shareQuoteAsImage(context: Context, bitmap: Bitmap) {
        val uri = saveImageToCache(context, bitmap)
        uri?.let {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, it)
                type = "image/png"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "Share Quote Card"))
        }
    }
    
    /**
     * Save quote card image to device gallery
     * 
     * @param context Application context
     * @param bitmap Bitmap to save
     * @return Uri of saved image, or null if failed
     */
    suspend fun saveImageToGallery(
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
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            
            // Mark as not pending (Android 10+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                context.contentResolver.update(uri, contentValues, null, null)
            }
            
            // Notify media scanner
            val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri)
            context.sendBroadcast(intent)
            
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
