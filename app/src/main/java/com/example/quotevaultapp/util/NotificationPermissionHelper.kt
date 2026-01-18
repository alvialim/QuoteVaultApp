package com.example.quotevaultapp.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.util.Log

/**
 * Helper class for requesting notification permission on Android 13+ (API 33+)
 * Provides convenient methods to check and request POST_NOTIFICATIONS permission
 * 
 * Designed for Jetpack Compose apps using ComponentActivity
 */
object NotificationPermissionHelper {
    
    private const val TAG = "NotificationPermissionHelper"
    
    /**
     * Check if notification permission is granted
     * 
     * @param context Application context
     * @return true if permission is granted or Android version is below 13, false otherwise
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Permission is automatically granted on Android 12 and below
            true
        }
    }
    
    /**
     * Check if notification permission should be requested
     * 
     * @param context Application context
     * @return true if permission should be requested (Android 13+ and not granted), false otherwise
     */
    fun shouldRequestPermission(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !hasNotificationPermission(context)
    }
    
    /**
     * Create an ActivityResultLauncher for requesting notification permission in a ComponentActivity
     * 
     * @param activity ComponentActivity that will handle the permission result
     * @param onPermissionResult Callback invoked with the permission result (true = granted, false = denied)
     * @return ActivityResultLauncher for requesting permission
     */
    fun createPermissionLauncher(
        activity: ComponentActivity,
        onPermissionResult: (Boolean) -> Unit
    ): ActivityResultLauncher<String> {
        return activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            Log.d(TAG, "Notification permission result: $isGranted")
            onPermissionResult(isGranted)
        }
    }
    
    /**
     * Request notification permission using the provided launcher
     * 
     * @param launcher ActivityResultLauncher for requesting permission
     * @param context Context to check if permission should be requested
     */
    fun requestPermission(
        launcher: ActivityResultLauncher<String>,
        context: Context
    ) {
        if (shouldRequestPermission(context)) {
            Log.d(TAG, "Requesting notification permission")
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            Log.d(TAG, "Notification permission not needed (Android 12 or below, or already granted)")
        }
    }
}

/**
 * Extension function to check if notification permission is granted
 * Convenient utility for Context objects
 * 
 * @return true if permission is granted or Android version is below 13, false otherwise
 */
fun Context.hasNotificationPermission(): Boolean {
    return NotificationPermissionHelper.hasNotificationPermission(this)
}