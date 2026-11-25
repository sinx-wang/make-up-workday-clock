package com.example.ui.screens

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.local.AutoSyncInterval
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePalette
import com.example.data.model.AlarmEntity
import com.example.ui.components.OneUiCard
import com.example.ui.i18n.AppLanguage
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalOneUiColors
import com.example.ui.viewmodel.HolidayAlarmUiState

@Composable
fun SettingsScreen(
    uiState: HolidayAlarmUiState,
    canScheduleExactAlarms: Boolean,
    onSetThemeMode: (ThemeMode) -> Unit,
    onSetThemePalette: (ThemePalette) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onSetAppLanguage: (AppLanguage) -> Unit,
    onSetAutoSyncInterval: (AutoSyncInterval) -> Unit,
    onSetOneUiExpandedHeader: (Boolean) -> Unit,
    onResetAllOverrides: () -> Unit,
    onTestRingPreview: (AlarmEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val oneUi = LocalOneUiColors.current
    val haptics = LocalHapticFeedback.current
    val s = LocalAppStrings.current

    var showLanguageDialog by remember { mutableStateOf(false) }

    var refreshTrigger by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshTrigger += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        refreshTrigger += 1
    }

    val hasNotificationPermission = remember(refreshTrigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    val isBatteryUnrestricted = remember(refreshTrigger) {
        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        pm?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    }

    val hasFullScreenIntent = remember(refreshTrigger) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.canUseFullScreenIntent() ?: true
        } else true
    }

    val grantedCount = listOf(
        canScheduleExactAlarms,
        isBatteryUnrestricted,
        hasNotificationPermission,
        hasFullScreenIntent
    ).count { it }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Card 1: Required Permissions
        item(key = "permissions_section_card") {
            SettingsSectionCard(
                icon = Icons.Rounded.VerifiedUser,
                title = s.permissionsTitle,
                trailing = {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (grantedCount == 4) oneUi.activeContainer else oneUi.lieuContainer
                    ) {
                        Text(
                            text = if (grantedCount == 4) s.permissionsReady else "$grantedCount/4",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (grantedCount == 4) oneUi.activeAccent else oneUi.lieuAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            ) {
                PermissionCheckRow(
                    icon = Icons.Rounded.Alarm,
                    title = s.permExactAlarm,
                    isGranted = canScheduleExactAlarms,
                    grantedLabel = s.permGranted,
                    actionLabel = s.permEnable,
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    }
                )
                SettingsCardDivider()
                PermissionCheckRow(
                    icon = Icons.Rounded.BatteryChargingFull,
                    title = s.permBatteryUnrestricted,
                    isGranted = isBatteryUnrestricted,
                    grantedLabel = s.permGranted,
                    actionLabel = s.permEnable,
                    onAction = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        runCatching { context.startActivity(intent) }.onFailure {
                            runCatching {
                                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                            }
                        }
                    }
                )
                SettingsCardDivider()
                PermissionCheckRow(
                    icon = Icons.Rounded.NotificationsActive,
                    title = s.permNotifications,
                    isGranted = hasNotificationPermission,
                    grantedLabel = s.permGranted,
                    actionLabel = s.permEnable,
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    }
                )
                SettingsCardDivider()
                PermissionCheckRow(
                    icon = Icons.Rounded.Fullscreen,
                    title = s.permFullScreen,
                    isGranted = hasFullScreenIntent,
                    grantedLabel = s.permGranted,
                    actionLabel = s.permEnable,
                    onAction = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            runCatching { context.startActivity(intent) }
                        }
                    }
                )
            }
        }

        // Card 2: Appearance (Theme Mode + Palette + Dynamic Color + Language + Expanded Header)
        item(key = "appearance_section_card") {
            SettingsSectionCard(
                icon = Icons.Rounded.Palette,
                title = s.appearanceTitle
            ) {
                // Theme Mode
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = s.themeModeTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = uiState.preferences.themeMode == mode,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetThemeMode(mode)
                                },
                                label = { Text(s.themeModeLabel(mode)) }
                            )
                        }
                    }
                }

                SettingsCardDivider()

                // Theme Color Palette Picker
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = s.themePaletteTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (uiState.preferences.useDynamicColor) s.followWallpaper
                            else s.themePaletteName(uiState.preferences.themePalette),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ThemePalette.entries.forEach { palette ->
                            val isSelected = !uiState.preferences.useDynamicColor &&
                                uiState.preferences.themePalette == palette
                            val swatchColor = Color(palette.seedColorHex)
                            val paletteLabel = s.themePaletteName(palette)

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        onSetThemePalette(palette)
                                    }
                                    .padding(vertical = 2.dp, horizontal = 2.dp)
                                    .testTag("palette_${palette.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .then(
                                            if (isSelected) {
                                                Modifier.border(
                                                    width = 2.dp,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    shape = CircleShape
                                                )
                                            } else Modifier
                                        )
                                        .padding(if (isSelected) 3.dp else 0.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = paletteLabel,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = paletteLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                SettingsCardDivider()

                SettingsSwitchRow(
                    title = s.dynamicColorTitle,
                    checked = uiState.preferences.useDynamicColor,
                    onCheckedChange = onSetDynamicColor
                )

                SettingsCardDivider()

                SettingsSwitchRow(
                    title = s.expandedHeaderTitle,
                    checked = uiState.preferences.oneUiExpandedHeader,
                    onCheckedChange = onSetOneUiExpandedHeader
                )

                SettingsCardDivider()

                // Extensible Language Picker Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            showLanguageDialog = true
                        }
                        .padding(horizontal = 18.dp)
                        .testTag("language_selector_row"),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = s.languageTitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = s.languageOptionLabel(uiState.preferences.appLanguage),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            contentDescription = s.languageTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Card 3: Auto Holiday Sync
        item(key = "sync_section_card") {
            SettingsSectionCard(
                icon = Icons.Rounded.CloudSync,
                title = s.autoSyncTitle
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AutoSyncInterval.entries.forEach { interval ->
                            FilterChip(
                                selected = uiState.preferences.autoSyncInterval == interval,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSetAutoSyncInterval(interval)
                                },
                                label = { Text(s.autoSyncLabel(interval)) }
                            )
                        }
                    }

                    if (uiState.userOverrideCount > 0) {
                        OutlinedButton(
                            onClick = onResetAllOverrides,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(23.dp)
                        ) {
                            Icon(Icons.Rounded.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(s.resetOverridesButton(uiState.userOverrideCount))
                        }
                    }

                    Button(
                        onClick = {
                            onTestRingPreview(
                                AlarmEntity(
                                    id = 0L,
                                    hour = 7,
                                    minute = 30,
                                    label = s.testRingSampleLabel,
                                    vibrate = true,
                                    gradualVolume = true,
                                    snoozeMinutes = 5
                                )
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("settings_test_ring_button"),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(Icons.Rounded.PlayCircleOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(s.testRingScreenButton)
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguagePickerDialog(
            selectedLanguage = uiState.preferences.appLanguage,
            onDismiss = { showLanguageDialog = false },
            onSelectLanguage = { lang ->
                onSetAppLanguage(lang)
                showLanguageDialog = false
            }
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    selectedLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    val s = LocalAppStrings.current
    val haptics = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(s.languageDialogTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AppLanguage.entries.forEach { lang ->
                    val selected = selectedLanguage == lang
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelectLanguage(lang)
                            }
                            .testTag("language_option_${lang.name.lowercase()}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selected,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onSelectLanguage(lang)
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = s.languageOptionLabel(lang),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(s.cancel)
            }
        }
    )
}

@Composable
private fun SettingsSectionCard(
    icon: ImageVector,
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    OneUiCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 28.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            trailing?.invoke()
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content
        )
    }
}

@Composable
private fun SettingsCardDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 18.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(!checked)
            }
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Switch(
            checked = checked,
            onCheckedChange = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(it)
            }
        )
    }
}

@Composable
private fun PermissionCheckRow(
    icon: ImageVector,
    title: String,
    isGranted: Boolean,
    grantedLabel: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    val oneUi = LocalOneUiColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(
                if (!isGranted) Modifier.clickable(onClick = onAction) else Modifier
            )
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isGranted) oneUi.activeAccent else oneUi.lieuAccent,
            modifier = Modifier.size(21.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))

        val pillBg = if (isGranted) oneUi.activeContainer else MaterialTheme.colorScheme.primaryContainer
        val pillFg = if (isGranted) oneUi.activeAccent else MaterialTheme.colorScheme.primary
        val pillIcon = if (isGranted) Icons.Rounded.CheckCircle else Icons.Rounded.WarningAmber
        val pillText = if (isGranted) grantedLabel else actionLabel

        Surface(
            modifier = Modifier
                .width(88.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(50))
                .then(if (!isGranted) Modifier.clickable(onClick = onAction) else Modifier),
            shape = RoundedCornerShape(50),
            color = pillBg
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = pillIcon,
                    contentDescription = pillText,
                    tint = if (isGranted) oneUi.activeAccent else oneUi.lieuAccent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = pillText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = pillFg
                )
            }
        }
    }
}
