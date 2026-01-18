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
 * 
 * IMPORTANT: This will ALWAYS request runtime permission if not granted before saving.
 * The system permission dialog will appear when user clicks save if permission is not granted.
 * 
 * Handles different Android versions appropriately:
 * - Android 13+: Requests READ_MEDIA_IMAGES permission
 * - Android < 13: Requests WRITE_EXTERNAL_STORAGE permission
 * 
 * Flow:
 * 1. User clicks Save → This function is called
 * 2. Check if permission is granted
 * 3. If NOT granted → Show system permission dialog (runtime request)
 * 4. If user grants → onGranted() is called → Save image
 * 5. If user denies → onDenied() is called → Show error message
 * 
 * @param onGranted Callback when permission is granted - will be called after user grants permission
 * @param onDenied Callback when permission is denied - will be called if user denies permission
 * @return Function to request permission - call this to show permission dialog before saving
 */
@Composable
fun rememberStoragePermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
): () -> Unit {
    val context = LocalContext.current
    
    // For Android 13+, request READ_MEDIA_IMAGES (or WRITE_EXTERNAL_STORAGE for saving)
    // For Android < 13, request WRITE_EXTERNAL_STORAGE to save images to gallery
    // Note: Even Android 10-12 may need WRITE_EXTERNAL_STORAGE for MediaStore operations
    val permission = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> Manifest.permission.READ_MEDIA_IMAGES
        else -> Manifest.permission.WRITE_EXTERNAL_STORAGE // Request for all versions < 13
    }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        android.util.Log.d("PermissionHelper", "Permission request result: isGranted=$isGranted")
        if (isGranted) {
            android.util.Log.d("PermissionHelper", "User granted permission, calling onGranted()")
            onGranted()
        } else {
            android.util.Log.w("PermissionHelper", "User denied permission, calling onDenied()")
            onDenied()
        }
    }
    
    return remember(permission, launcher, context) {
        {
            android.util.Log.d("PermissionHelper", "Requesting storage permission. Android version: ${Build.VERSION.SDK_INT}, Permission: $permission")
            
            // Always check permission status at CALL TIME (not composition time)
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                permission
            ) == PackageManager.PERMISSION_GRANTED
            
            android.util.Log.d("PermissionHelper", "Permission $permission status: granted=$hasPermission")
            
            if (hasPermission) {
                // Permission already granted, proceed immediately with save
                android.util.Log.d("PermissionHelper", "Permission already granted, proceeding with save")
                onGranted()
            } else {
                // Permission NOT granted - MUST request it at runtime BEFORE saving
                android.util.Log.d("PermissionHelper", "Permission NOT granted, requesting runtime permission NOW - dialog should appear")
                try {
                    // Launch system permission dialog - this will show the Android permission popup
                    launcher.launch(permission)
                    android.util.Log.d("PermissionHelper", "Permission launcher.launch() called successfully")
                    // onGranted() will be called automatically by the launcher callback if user grants permission
                    // onDenied() will be called if user denies permission
                } catch (e: Exception) {
                    android.util.Log.e("PermissionHelper", "Failed to launch permission request: ${e.message}", e)
                    e.printStackTrace()
                    onDenied()
                }
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
