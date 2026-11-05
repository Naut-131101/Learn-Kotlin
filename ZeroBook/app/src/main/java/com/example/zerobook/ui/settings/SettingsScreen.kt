package com.example.zerobook.ui.settings

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.zerobook.BuildConfig
import com.example.zerobook.R
import com.example.zerobook.model.ThemeMode
import kotlinx.coroutines.launch
import java.util.Locale

@StringRes
private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val permissionDeniedMessage = stringResource(R.string.settings_notification_permission_denied)
    val syncStartedMessage = stringResource(R.string.settings_sync_started)

    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    // Runtime permission: từ Android 13 phải xin quyền trước khi gửi thông báo
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.setReminderEnabled(true)
        } else {
            scope.launch { snackbarHostState.showSnackbar(permissionDeniedMessage) }
        }
    }

    SettingsContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onThemeModeChange = viewModel::setThemeMode,
        onReminderToggle = { enabled ->
            if (enabled && needsNotificationPermission(context)) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setReminderEnabled(enabled)
            }
        },
        onReminderTimeClick = { showTimePicker = true },
        onLanguageClick = { openAppLanguageSettings(context) },
        onSyncNow = {
            viewModel.syncNow()
            scope.launch { snackbarHostState.showSnackbar(syncStartedMessage) }
        },
        onSignOutClick = { showSignOutDialog = true },
    )

    if (showTimePicker) {
        ReminderTimeDialog(
            initialHour = uiState.preferences.reminderHour,
            initialMinute = uiState.preferences.reminderMinute,
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                viewModel.setReminderTime(hour, minute)
                showTimePicker = false
            },
        )
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.settings_sign_out_dialog_title)) },
            text = { Text(stringResource(R.string.settings_sign_out_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    viewModel.signOut()
                }) { Text(stringResource(R.string.settings_sign_out)) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onThemeModeChange: (ThemeMode) -> Unit,
    onReminderToggle: (Boolean) -> Unit,
    onReminderTimeClick: () -> Unit,
    onLanguageClick: () -> Unit,
    onSyncNow: () -> Unit,
    onSignOutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val prefs = uiState.preferences
    val user = uiState.user

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.nav_settings)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ---- Tài khoản ----
            SectionHeader(R.string.settings_section_account)
            val email = user?.email
            val photoUrl = user?.photoUrl
            ListItem(
                headlineContent = { Text(user?.displayName ?: email.orEmpty()) },
                supportingContent = if (email != null) {
                    { Text(email) }
                } else {
                    null
                },
                leadingContent = {
                    if (photoUrl != null) {
                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Icon(Icons.Filled.AccountCircle, null, Modifier.size(40.dp))
                    }
                },
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_sync_now)) },
                supportingContent = { Text(stringResource(R.string.settings_sync_description)) },
                leadingContent = { Icon(Icons.Filled.Sync, null) },
                modifier = Modifier.clickable(onClick = onSyncNow),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_sign_out)) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                modifier = Modifier.clickable(onClick = onSignOutClick),
            )
            HorizontalDivider()

            // ---- Giao diện ----
            SectionHeader(R.string.settings_section_appearance)
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    ListItem(
                        headlineContent = { Text(stringResource(mode.labelRes())) },
                        leadingContent = {
                            // onClick = null: cả dòng đã bấm được nhờ Modifier.selectable
                            RadioButton(selected = prefs.themeMode == mode, onClick = null)
                        },
                        modifier = Modifier.selectable(
                            selected = prefs.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            role = Role.RadioButton,
                        ),
                    )
                }
            }
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_language)) },
                supportingContent = { Text(stringResource(R.string.settings_language_description)) },
                leadingContent = { Icon(Icons.Filled.Language, null) },
                modifier = Modifier.clickable(onClick = onLanguageClick),
            )
            HorizontalDivider()

            // ---- Nhắc đọc sách ----
            SectionHeader(R.string.settings_section_reminder)
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_reminder_daily)) },
                supportingContent = { Text(stringResource(R.string.settings_reminder_description)) },
                leadingContent = { Icon(Icons.Filled.Notifications, null) },
                trailingContent = { Switch(checked = prefs.reminderEnabled, onCheckedChange = null) },
                modifier = Modifier.toggleable(
                    value = prefs.reminderEnabled,
                    onValueChange = onReminderToggle,
                    role = Role.Switch,
                ),
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_reminder_time)) },
                supportingContent = {
                    Text(String.format(Locale.ROOT, "%02d:%02d", prefs.reminderHour, prefs.reminderMinute))
                },
                leadingContent = { Icon(Icons.Filled.Schedule, null) },
                modifier = Modifier.clickable(
                    enabled = prefs.reminderEnabled,
                    onClick = onReminderTimeClick,
                ),
            )
            HorizontalDivider()

            // ---- Giới thiệu ----
            SectionHeader(R.string.settings_section_about)
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_name)) },
                supportingContent = {
                    Text(stringResource(R.string.settings_version, BuildConfig.VERSION_NAME))
                },
            )
        }
    }
}

@Composable
private fun SectionHeader(@StringRes titleRes: Int) {
    Text(
        text = stringResource(titleRes),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_reminder_time)) },
        text = { TimeInput(state = state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

private fun needsNotificationPermission(context: Context): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
        PackageManager.PERMISSION_GRANTED

/** Android 13+: mở trang chọn ngôn ngữ riêng cho app. Máy cũ hơn: mở cài đặt ngôn ngữ hệ thống. */
private fun openAppLanguageSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Intent(Settings.ACTION_APP_LOCALE_SETTINGS, Uri.fromParts("package", context.packageName, null))
    } else {
        Intent(Settings.ACTION_LOCALE_SETTINGS)
    }
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Settings.ACTION_SETTINGS))
    }
}
