package com.example.quotevaultapp.presentation.settings

import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.quotevaultapp.util.NotificationPermissionHelper
import com.example.quotevaultapp.BuildConfig
import com.example.quotevaultapp.domain.model.AppTheme
import com.example.quotevaultapp.domain.model.FontSize
import com.example.quotevaultapp.domain.model.Quote
import com.example.quotevaultapp.domain.model.QuoteCategory
import com.example.quotevaultapp.presentation.components.NotificationPermissionDialog
import com.example.quotevaultapp.presentation.components.QuoteCard
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Settings screen for app configuration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    preferencesManager: com.example.quotevaultapp.data.local.PreferencesManager? = null,
    authRepository: com.example.quotevaultapp.domain.repository.AuthRepository? = null,
    viewModel: SettingsViewModel? = null
) {
    val context = LocalContext.current
    
    // Get singleton instances if not provided
    val prefsManager = preferencesManager ?: remember {
        com.example.quotevaultapp.data.local.PreferencesManager.getInstance(context)
    }
    val authRepo = authRepository ?: remember {
        com.example.quotevaultapp.data.remote.supabase.SupabaseAuthRepository()
    }
    
    val settingsViewModel: SettingsViewModel = viewModel ?: viewModel(
        factory = SettingsViewModelFactory(prefsManager, authRepo)
    )
    val currentUser by settingsViewModel.currentUser.collectAsState()
    val theme by settingsViewModel.theme.collectAsState()
    val fontSize by settingsViewModel.fontSize.collectAsState()
    val notificationEnabled by settingsViewModel.notificationEnabled.collectAsState()
    val notificationHour by settingsViewModel.notificationHour.collectAsState()
    val notificationMinute by settingsViewModel.notificationMinute.collectAsState()
    val nextScheduledTime by settingsViewModel.nextScheduledTime.collectAsState()
    val uiState by settingsViewModel.uiState.collectAsState()
    
    var showEditDisplayNameDialog by remember { mutableStateOf(false) }
    var showTimePickerDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showPermissionDialogForTest by remember { mutableStateOf(false) }
    var editDisplayName by remember { mutableStateOf("") }
    
    // Update next scheduled time when screen is displayed or notification enabled changes
    LaunchedEffect(notificationEnabled) {
        if (notificationEnabled) {
            settingsViewModel.updateNextScheduledTime(context)
        }
    }
    
    // Handle UI state changes
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SettingsUiState.Success -> {
                // Could show snackbar here
                showEditDisplayNameDialog = false
            }
            is SettingsUiState.Error -> {
                // Could show error snackbar here
            }
            is SettingsUiState.SignedOut -> {
                // Navigate to login screen (handled by navigation)
                onBack()
            }
            is SettingsUiState.Loading -> {
                // Show loading state
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance") {
                // Theme Selection
                SettingsItem(
                    title = "Theme",
                    subtitle = when (theme) {
                        AppTheme.LIGHT -> "Light"
                        AppTheme.DARK -> "Dark"
                        AppTheme.SYSTEM -> "System Default"
                    },
                    onClick = {
                        val themes = AppTheme.values()
                        val currentIndex = themes.indexOf(theme)
                        val nextIndex = (currentIndex + 1) % themes.size
                        settingsViewModel.setTheme(themes[nextIndex])
                    }
                )
                
                Divider()
                
                // Font Size Selection
                SettingsItem(
                    title = "Font Size",
                    subtitle = when (fontSize) {
                        FontSize.SMALL -> "Small"
                        FontSize.MEDIUM -> "Medium"
                        FontSize.LARGE -> "Large"
                    },
                    onClick = {
                        val sizes = FontSize.values()
                        val currentIndex = sizes.indexOf(fontSize)
                        val nextIndex = (currentIndex + 1) % sizes.size
                        settingsViewModel.setFontSize(sizes[nextIndex])
                    }
                )
                
                Divider()
                
                // Preview
                Text(
                    text = "Preview",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    QuoteCard(
                        quote = Quote(
                            id = "preview",
                            text = "The only way to do great work is to love what you do.",
                            author = "Steve Jobs",
                            category = QuoteCategory.MOTIVATION,
                            createdAt = System.currentTimeMillis(),
                            isFavorite = false
                        ),
                        onFavoriteClick = {},
                        onShareClick = {},
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            
            // Notifications Section
            SettingsSection(title = "Notifications") {
                // Enable/Disable notification toggle
                SettingsItemWithSwitch(
                    title = "Daily Quote Notification",
                    subtitle = if (notificationEnabled) {
                        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, notificationHour)
                            set(Calendar.MINUTE, notificationMinute)
                        }
                        "Daily at ${timeFormat.format(calendar.time)}"
                    } else {
                        "Get inspired daily with a quote"
                    },
                    checked = notificationEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled) {
                            // Check if permission is needed (Android 13+)
                            if (NotificationPermissionHelper.shouldRequestPermission(context)) {
                                // Show permission dialog
                                showPermissionDialog = true
                            } else {
                                // Permission already granted or not needed (Android 12-)
                                settingsViewModel.updateNotificationEnabled(true, context)
                                showTimePickerDialog = true
                            }
                        } else {
                            // Disable notifications
                            settingsViewModel.updateNotificationEnabled(false, context)
                        }
                    },
                    onItemClick = {
                        if (notificationEnabled) {
                            showTimePickerDialog = true
                        }
                    }
                )
                
                // Time picker (only visible when notifications are enabled)
                if (notificationEnabled) {
                    Divider()
                    
                    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
                    val notificationTimeString = remember(notificationHour, notificationMinute) {
                        val calendar = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, notificationHour)
                            set(Calendar.MINUTE, notificationMinute)
                        }
                        timeFormat.format(calendar.time)
                    }
                    
                    SettingsItem(
                        title = "Notification Time",
                        subtitle = notificationTimeString,
                        onClick = {
                            showTimePickerDialog = true
                        }
                    )
                    
                    // Show next scheduled notification time
                    if (nextScheduledTime != null) {
                        Divider()
                        SettingsItem(
                            title = "Next Notification",
                            subtitle = nextScheduledTime ?: "Not scheduled"
                        )
                    }
                    
                    Divider()
                    
                    // Test notification button
                    Button(
                        onClick = {
                            // Check permission first
                            if (NotificationPermissionHelper.shouldRequestPermission(context)) {
                                showPermissionDialogForTest = true
                            } else {
                                settingsViewModel.sendTestNotification(context)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Send Test Notification")
                    }
                }
            }
            
            // Account Section
            if (currentUser != null) {
                SettingsSection(title = "Account") {
                    // Display Name
                    SettingsItem(
                        title = "Display Name",
                        subtitle = currentUser?.displayName ?: "Not set",
                        onClick = {
                            editDisplayName = currentUser?.displayName ?: ""
                            showEditDisplayNameDialog = true
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    
                    Divider()
                    
                    // Avatar (placeholder - can be expanded)
                    SettingsItem(
                        title = "Profile Picture",
                        subtitle = "Tap to change",
                        onClick = {
                            // TODO: Implement image picker
                        }
                    )
                    
                    Divider()
                    
                    // Logout
                    SettingsItem(
                        title = "Sign Out",
                        subtitle = currentUser?.email ?: "",
                        onClick = {
                            showLogoutDialog = true
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Sign Out",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        textColor = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // About Section
            SettingsSection(title = "About") {
                SettingsItem(
                    title = "Version",
                    subtitle = BuildConfig.VERSION_NAME
                )
                
                Divider()
                
                SettingsItem(
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/privacy"))
                        context.startActivity(intent)
                    }
                )
                
                Divider()
                
                SettingsItem(
                    title = "Terms of Service",
                    subtitle = "View terms and conditions",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://example.com/terms"))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
    
    // Edit Display Name Dialog
    if (showEditDisplayNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditDisplayNameDialog = false },
            title = { Text("Edit Display Name") },
            text = {
                OutlinedTextField(
                    value = editDisplayName,
                    onValueChange = { editDisplayName = it },
                    label = { Text("Display Name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.updateDisplayName(editDisplayName.trim())
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDisplayNameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Time Picker Dialog
    if (showTimePickerDialog) {
        val calendar = Calendar.getInstance()
        TimePickerDialog(
            context,
            { _, hour, minute ->
                        settingsViewModel.updateNotificationTime(hour, minute, context)
                showTimePickerDialog = false
            },
            notificationHour,
            notificationMinute,
            false
        ).show()
    }
    
    // Notification Permission Dialog (for enabling notifications)
    if (showPermissionDialog) {
        NotificationPermissionDialog(
            onGranted = {
                showPermissionDialog = false
                // Enable notifications and schedule WorkManager
                settingsViewModel.updateNotificationEnabled(true, context)
                showTimePickerDialog = true
            },
            onDenied = {
                showPermissionDialog = false
                // User denied permission, keep notifications disabled
                // Optionally show a message explaining why permission is needed
            }
        )
    }
    
    // Notification Permission Dialog (for test notification)
    if (showPermissionDialogForTest) {
        NotificationPermissionDialog(
            onGranted = {
                showPermissionDialogForTest = false
                // Permission granted, now send test notification
                settingsViewModel.sendTestNotification(context)
            },
            onDenied = {
                showPermissionDialogForTest = false
                // User denied permission, cannot send test notification
            }
        )
    }
    
    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.signOut()
                        showLogoutDialog = false
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Settings section container
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

/**
 * Settings item (clickable)
 */
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable(
                            onClick = onClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(vertical = 8.dp)
                } else {
                    Modifier.padding(vertical = 8.dp)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        if (trailingIcon != null) {
            trailingIcon()
        } else if (onClick != null) {
            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Settings item with switch
 */
@Composable
private fun SettingsItemWithSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onItemClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
