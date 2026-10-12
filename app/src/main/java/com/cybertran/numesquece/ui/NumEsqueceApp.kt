package com.cybertran.numesquece.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cybertran.numesquece.BuildConfig
import com.cybertran.numesquece.R
import com.cybertran.numesquece.NotificationSoundMode
import com.cybertran.numesquece.ReminderViewModel
import com.cybertran.numesquece.SettingsStore
import com.cybertran.numesquece.data.ReminderEntity
import com.cybertran.numesquece.sync.GoogleDriveSyncManager
import com.cybertran.numesquece.sync.GoogleSyncState
import com.cybertran.numesquece.ui.theme.NumEsqueceTheme

internal enum class Destination { NOW, REMINDERS, HISTORY, SETTINGS }

@Composable
fun NumEsqueceRoot(
    viewModel: ReminderViewModel,
    settings: SettingsStore,
    googleSyncManager: GoogleDriveSyncManager,
    onConnectGoogle: () -> Unit,
    onSyncGoogle: () -> Unit,
    onDisconnectGoogle: () -> Unit,
) {
    var dynamicColor by remember { mutableStateOf(settings.dynamicColor) }
    var onboardingDone by remember { mutableStateOf(settings.onboardingDone) }
    var notificationSoundMode by remember { mutableStateOf(settings.notificationSoundMode) }
    val syncState by googleSyncManager.state.collectAsState()

    NumEsqueceTheme(dynamicColor = dynamicColor) {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!onboardingDone) {
                OnboardingScreen(
                    syncState = syncState,
                    onConnectGoogle = onConnectGoogle,
                    onFinished = {
                        settings.onboardingDone = true
                        onboardingDone = true
                    },
                )
            } else {
                NumEsqueceApp(
                    viewModel = viewModel,
                    dynamicColor = dynamicColor,
                    onDynamicColorChanged = {
                        settings.dynamicColor = it
                        dynamicColor = it
                    },
                    notificationSoundMode = notificationSoundMode,
                    onNotificationSoundModeChanged = {
                        settings.notificationSoundMode = it
                        notificationSoundMode = it
                    },
                    syncState = syncState,
                    onConnectGoogle = onConnectGoogle,
                    onSyncGoogle = onSyncGoogle,
                    onDisconnectGoogle = onDisconnectGoogle,
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreen(
    syncState: GoogleSyncState,
    onConnectGoogle: () -> Unit,
    onFinished: () -> Unit,
) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val syncBusy = syncState.phase == com.cybertran.numesquece.sync.SyncPhase.CONNECTING ||
        syncState.phase == com.cybertran.numesquece.sync.SyncPhase.SYNCING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(36.dp),
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(
            stringResource(R.string.onboarding_tagline),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
        )

        OnboardingPoint(
            title = stringResource(R.string.onboarding_fast_title),
            body = stringResource(R.string.onboarding_fast_body),
            icon = Icons.Rounded.Add,
        )
        OnboardingPoint(
            title = stringResource(R.string.onboarding_intensity_title),
            body = stringResource(R.string.onboarding_intensity_body),
            icon = Icons.Rounded.Alarm,
        )
        if (BuildConfig.GOOGLE_SYNC_AVAILABLE) {
            OnboardingPoint(
                title = stringResource(R.string.onboarding_account_title),
                body = stringResource(R.string.onboarding_account_body),
                icon = Icons.Rounded.AccountCircle,
            )

            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.sync_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                stringResource(R.string.onboarding_sync_body),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 10.dp),
            )

            OutlinedButton(
                onClick = onConnectGoogle,
                enabled = !syncBusy && !syncState.connected,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        syncState.connected -> stringResource(R.string.google_account_connected)
                        syncBusy -> stringResource(R.string.connecting)
                        else -> stringResource(R.string.connect_google_account)
                    }
                )
            }

            syncState.message?.let { message ->
                Text(
                    message,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (syncState.phase == com.cybertran.numesquece.sync.SyncPhase.ERROR) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Text(
                stringResource(R.string.onboarding_sync_later),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        } else {
            OnboardingPoint(
                title = stringResource(R.string.onboarding_local_title),
                body = stringResource(R.string.onboarding_local_body),
                icon = Icons.Rounded.CheckCircle,
            )
        }

        Spacer(Modifier.height(22.dp))
        Button(
            onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) { Text(stringResource(R.string.enable_notifications)) }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                        Uri.parse("package:" + context.packageName),
                    )
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(stringResource(R.string.allow_exact_alarms)) }
        }

        Spacer(Modifier.height(18.dp))
        Button(onClick = onFinished, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.start)) }
        Text(
            stringResource(R.string.onboarding_options_later),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun OnboardingPoint(title: String, body: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun NumEsqueceApp(
    viewModel: ReminderViewModel,
    dynamicColor: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    notificationSoundMode: NotificationSoundMode,
    onNotificationSoundModeChanged: (NotificationSoundMode) -> Unit,
    syncState: GoogleSyncState,
    onConnectGoogle: () -> Unit,
    onSyncGoogle: () -> Unit,
    onDisconnectGoogle: () -> Unit,
) {
    val reminders by viewModel.reminders.collectAsState()
    var destination by remember { mutableStateOf(Destination.NOW) }
    var editingReminder by remember { mutableStateOf<ReminderEntity?>(null) }
    var editorOpen by remember { mutableStateOf(false) }

    if (editorOpen) {
        ReminderEditorScreen(
            reminder = editingReminder,
            onBack = { editorOpen = false },
            onSave = {
                viewModel.save(it)
                editorOpen = false
            },
            onDelete = editingReminder?.let { reminder ->
                {
                    viewModel.delete(reminder.id)
                    editorOpen = false
                }
            },
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            AppTopBar(
                destination = destination,
                onSettings = { destination = Destination.SETTINGS },
                onBackFromSettings = { destination = Destination.NOW },
            )
        },
        bottomBar = {
            if (destination != Destination.SETTINGS) {
                NavigationBar {
                    NavigationBarItem(
                        selected = destination == Destination.NOW,
                        onClick = { destination = Destination.NOW },
                        icon = { Icon(Icons.Rounded.Home, null) },
                        label = { Text(stringResource(R.string.nav_now)) },
                    )
                    NavigationBarItem(
                        selected = destination == Destination.REMINDERS,
                        onClick = { destination = Destination.REMINDERS },
                        icon = { Icon(Icons.Rounded.Schedule, null) },
                        label = { Text(stringResource(R.string.nav_reminders)) },
                    )
                    NavigationBarItem(
                        selected = destination == Destination.HISTORY,
                        onClick = { destination = Destination.HISTORY },
                        icon = { Icon(Icons.Rounded.History, null) },
                        label = { Text(stringResource(R.string.nav_history)) },
                    )
                }
            }
        },
        floatingActionButton = {
            if (destination != Destination.SETTINGS && destination != Destination.HISTORY) {
                ExtendedFloatingActionButton(
                    onClick = {
                        editingReminder = null
                        editorOpen = true
                    },
                    icon = { Icon(Icons.Rounded.Add, null) },
                    text = { Text(stringResource(R.string.remember_action)) },
                )
            }
        },
    ) { padding ->
        when (destination) {
            Destination.NOW -> NowScreen(
                reminders = reminders,
                padding = padding,
                onComplete = viewModel::complete,
                onSnooze = { viewModel.snooze(it, 10) },
                onOpen = {
                    editingReminder = it
                    editorOpen = true
                },
            )
            Destination.REMINDERS -> ReminderListScreen(
                reminders = reminders,
                padding = padding,
                onOpen = {
                    editingReminder = it
                    editorOpen = true
                },
            )
            Destination.HISTORY -> HistoryScreen(reminders, padding)
            Destination.SETTINGS -> SettingsScreen(
                padding = padding,
                dynamicColor = dynamicColor,
                onDynamicColorChanged = onDynamicColorChanged,
                onTestReminder = viewModel::createTestReminder,
                notificationSoundMode = notificationSoundMode,
                onNotificationSoundModeChanged = onNotificationSoundModeChanged,
                syncState = syncState,
                onConnectGoogle = onConnectGoogle,
                onSyncGoogle = onSyncGoogle,
                onDisconnectGoogle = onDisconnectGoogle,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    destination: Destination,
    onSettings: () -> Unit,
    onBackFromSettings: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                when (destination) {
                    Destination.NOW -> stringResource(R.string.app_name)
                    Destination.REMINDERS -> stringResource(R.string.nav_reminders)
                    Destination.HISTORY -> stringResource(R.string.nav_history)
                    Destination.SETTINGS -> stringResource(R.string.settings)
                },
                fontWeight = FontWeight.Bold,
            )
        },
        navigationIcon = {
            if (destination == Destination.SETTINGS) {
                TextButton(onClick = onBackFromSettings) { Text(stringResource(R.string.back)) }
            }
        },
        actions = {
            if (destination != Destination.SETTINGS) {
                IconButton(onClick = onSettings) {
                    Icon(Icons.Rounded.Settings, contentDescription = stringResource(R.string.settings))
                }
            }
        },
    )
}
