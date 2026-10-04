package com.rhinepereira.saints.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rhinepereira.saints.R
import kotlinx.coroutines.launch

private fun Context.notificationsAllowed(): Boolean =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        NotificationManagerCompat.from(this).areNotificationsEnabled()
    }

/**
 * Explains, then asks for, notification permission. Shows once on first launch when [askOnFirstLaunch];
 * the returned lambda is the bell action: it confirms, explains, or sends the user to settings.
 */
@Composable
fun rememberNotificationGate(snackbar: SnackbarHostState, askOnFirstLaunch: Boolean = false): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var shownOnce by rememberSaveable { mutableStateOf(false) }
    var requested by rememberSaveable { mutableStateOf(false) }
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scope.launch { snackbar.showSnackbar(context.getString(R.string.notification_enabled_snackbar)) }
    }

    fun request() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val rationale = activity?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) ?: false
        if (requested && !rationale) {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } else {
            requested = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        if (askOnFirstLaunch && !shownOnce && !prefs.getBoolean("asked_notifications", false) &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !context.notificationsAllowed()
        ) {
            shownOnce = true
            prefs.edit().putBoolean("asked_notifications", true).apply()
            showDialog = true
        }
    }

    if (showDialog) {
        NotificationPermissionDialog(
            onDismiss = { showDialog = false },
            onAllow = { showDialog = false; request() }
        )
    }

    return {
        when {
            context.notificationsAllowed() -> scope.launch {
                snackbar.showSnackbar(context.getString(R.string.notification_already_enabled_snackbar))
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> showDialog = true
            else -> scope.launch {
                snackbar.showSnackbar(context.getString(R.string.notification_disabled_settings_snackbar))
            }
        }
    }
}

@Composable
private fun NotificationPermissionDialog(onDismiss: () -> Unit, onAllow: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(stringResource(R.string.notification_permission_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.notification_permission_why), style = MaterialTheme.typography.bodyMedium)
                Text(stringResource(R.string.notification_permission_benefits_header), style = MaterialTheme.typography.titleSmall)
                listOf(
                    R.string.notification_permission_benefit_daily_readings,
                    R.string.notification_permission_benefit_feast_days,
                    R.string.notification_permission_benefit_spiritual
                ).forEach {
                    Text("·  " + stringResource(it), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = onAllow) { Text(stringResource(R.string.notification_permission_allow)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.notification_permission_later)) } }
    )
}
