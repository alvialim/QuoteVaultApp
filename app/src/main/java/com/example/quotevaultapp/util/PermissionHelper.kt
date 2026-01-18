package com.example.quotevaultapp.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Remember storage permission launcher for saving images to gallery
 * Handles different Android versions appropriately:
 * - Android 13+: READ_MEDIA_IMAGES
 * - Android 10-12: No permission needed (scoped storage)
 * - Android < 10: WRITE_EXTERNAL_STORAGE
 * 
 * @param onGranted Callback when permission is granted
 * @param onDenied Callback when permission is denied
 * @return Function to request permission
 */
@Composable
fun rememberStoragePermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    
    // For Android 13+, request READ_MEDIA_IMAGES
    // For Android < 10, request WRITE_EXTERNAL_STORAGE
    // For Android 10-12, no permission needed (scoped storage)
    val permission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_IMAGES
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q -> Manifest.permission.WRITE_EXTERNAL_STORAGE
        else -> null // No permission needed for Android 10-12
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGranted()
        } else {
            onDenied()
        }
    }
    
    return remember(permission, launcher) {
        {
            if (permission != null) {
                // Check if already granted
                val hasPermission = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                
                if (hasPermission) {
                    onGranted()
                } else {
                    launcher.launch(permission)
                }
            } else {
                // No permission needed (Android 10-12)
                onGranted()
            }
        }
    }
}

/**
 * Check if storage permission is granted
 * Returns true for Android 10-12 (scoped storage) or if permission is granted
 */
fun hasStoragePermission(context: android.content.Context): Boolean {
    return when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            // Android 10-12: No permission needed for scoped storage
            true
        }
        else -> {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}
