package com.example

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ResolvedDayInfo
import com.example.ui.components.AlarmEditorSheet
import com.example.ui.components.GroupEditorSheet
import com.example.ui.components.ManualBatchImportSheet
import com.example.ui.components.SingleDayOverrideSheet
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.StringPacks
import com.example.ui.screens.AlarmsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.HolidayAlarmTheme
import com.example.ui.viewmodel.HolidayAlarmViewModel

enum class AppDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    ALARMS("alarms", Icons.Rounded.Alarm, Icons.Outlined.Alarm),
    CALENDAR("calendar", Icons.Rounded.CalendarMonth, Icons.Outlined.CalendarMonth),
    SETTINGS("settings", Icons.Rounded.Tune, Icons.Outlined.Tune);

    fun localizedLabel(strings: AppStrings): String = when (this) {
        ALARMS -> strings.navAlarms
        CALENDAR -> strings.navCalendar
        SETTINGS -> strings.navSettings
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: HolidayAlarmViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val strings = remember(uiState.preferences.appLanguage) {
                StringPacks.forLanguage(uiState.preferences.appLanguage)
            }

            CompositionLocalProvider(LocalAppStrings provides strings) {
                HolidayAlarmTheme(
                    themeMode = uiState.preferences.themeMode,
                    themePalette = uiState.preferences.themePalette,
                    useDynamicColor = uiState.preferences.useDynamicColor
                ) {
                    HolidayAlarmRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
private fun HolidayAlarmRoot(viewModel: HolidayAlarmViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val strings = LocalAppStrings.current

    var currentTab by remember { mutableStateOf(AppDestination.ALARMS) }

    // Modal sheet states
    var editingAlarm by remember { mutableStateOf<AlarmEntity?>(null) }
    var showNewAlarmSheet by remember { mutableStateOf(false) }
    var newAlarmDefaultGroupId by remember { mutableStateOf<Long?>(null) }

    var editingGroup by remember { mutableStateOf<AlarmGroupEntity?>(null) }
    var showNewGroupSheet by remember { mutableStateOf(false) }

    var editingDayInfo by remember { mutableStateOf<ResolvedDayInfo?>(null) }
    var showManualImportSheet by remember { mutableStateOf(false) }

    // Predictive Back / BackHandler on non-home tabs returns smoothly to Alarms tab
    BackHandler(enabled = currentTab != AppDestination.ALARMS) {
        currentTab = AppDestination.ALARMS
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.consumeSnackbar()
        }
    }

    fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            runCatching { context.startActivity(intent) }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 680.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.statusBars,
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val label = dest.localizedLabel(strings)
                            NavigationBarItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("nav_${dest.route}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (currentTab == AppDestination.ALARMS) {
                    FloatingActionButton(
                        onClick = {
                            newAlarmDefaultGroupId = uiState.groups.firstOrNull()?.group?.id
                            showNewAlarmSheet = true
                        },
                        shape = RoundedCornerShape(22.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.testTag("fab_add_alarm")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = strings.newAlarm
                        )
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .windowInsetsPadding(WindowInsets.navigationBars),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        AppDestination.entries.forEach { dest ->
                            val selected = currentTab == dest
                            val label = dest.localizedLabel(strings)
                            NavigationRailItem(
                                selected = selected,
                                onClick = { currentTab = dest },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = label
                                    )
                                },
                                label = { Text(label) },
                                modifier = Modifier.testTag("rail_${dest.route}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)

                    when (currentTab) {
                        AppDestination.ALARMS -> AlarmsScreen(
                            uiState = uiState,
                            canScheduleExactAlarms = viewModel.canScheduleExactAlarms(),
                            onOpenExactAlarmSettings = ::openExactAlarmSettings,
                            onSyncHolidays = { viewModel.syncHolidaysNow() },
                            onAddAlarm = { groupId ->
                                newAlarmDefaultGroupId = groupId
                                showNewAlarmSheet = true
                            },
                            onEditAlarm = { alarm -> editingAlarm = alarm },
                            onToggleAlarm = { id, enabled -> viewModel.toggleAlarm(id, enabled) },
                            onToggleSkipNext = { item -> viewModel.toggleSkipNextOccurrence(item) },
                            onAddGroup = { showNewGroupSheet = true },
                            onEditGroup = { group -> editingGroup = group },
                            onToggleGroup = { id, enabled -> viewModel.toggleGroup(id, enabled) },
                            onToggleSkipNextGroup = { groupModel -> viewModel.toggleSkipNextForGroup(groupModel) },
                            modifier = contentModifier
                        )

                        AppDestination.CALENDAR -> CalendarScreen(
                            uiState = uiState,
                            onSyncYears = { years -> viewModel.syncHolidaysNow(years) },
                            onEditDayOverride = { dayInfo -> editingDayInfo = dayInfo },
                            onOpenManualImport = { showManualImportSheet = true },
                            modifier = contentModifier
                        )

                        AppDestination.SETTINGS -> SettingsScreen(
                            uiState = uiState,
                            canScheduleExactAlarms = viewModel.canScheduleExactAlarms(),
                            onSetThemeMode = viewModel::setThemeMode,
                            onSetThemePalette = viewModel::setThemePalette,
                            onSetDynamicColor = viewModel::setUseDynamicColor,
                            onSetAppLanguage = viewModel::setAppLanguage,
                            onSetAutoSyncInterval = viewModel::setAutoSyncInterval,
                            onSetOneUiExpandedHeader = viewModel::setOneUiExpandedHeader,
                            onResetAllOverrides = viewModel::resetAllManualOverrides,
                            onTestRingPreview = viewModel::testRingAlarm,
                            modifier = contentModifier
                        )
                    }
                }
            }
        }

        // Dock Snackbar cleanly at the very bottom of the app (One UI floating pill above system gesture bar)
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .widthIn(max = 560.dp)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                actionColor = MaterialTheme.colorScheme.inversePrimary
            )
        }
    }

    // Sheets
    if (showNewAlarmSheet || editingAlarm != null) {
        AlarmEditorSheet(
            initialAlarm = editingAlarm,
            defaultGroupId = newAlarmDefaultGroupId,
            groups = uiState.groups.map { it.group },
            onDismiss = {
                showNewAlarmSheet = false
                editingAlarm = null
            },
            onSave = { alarm -> viewModel.saveAlarm(alarm) },
            onDelete = editingAlarm?.let { existing ->
                { viewModel.deleteAlarm(existing) }
            },
            onTestRing = { alarm -> viewModel.testRingAlarm(alarm) }
        )
    }

    if (showNewGroupSheet || editingGroup != null) {
        GroupEditorSheet(
            initialGroup = editingGroup,
            onDismiss = {
                showNewGroupSheet = false
                editingGroup = null
            },
            onSave = { group -> viewModel.saveGroup(group) },
            onDelete = editingGroup?.let { existing ->
                { viewModel.deleteGroup(existing) }
            }
        )
    }

    editingDayInfo?.let { dayInfo ->
        SingleDayOverrideSheet(
            dayInfo = dayInfo,
            onDismiss = { editingDayInfo = null },
            onApplyOverride = { date, type, label ->
                viewModel.setManualHolidayOverride(date, type, label)
            },
            onApplyRange = { start, end, type, label ->
                viewModel.setManualHolidayRange(start, end, type, label)
            },
            onRevertOfficial = { date ->
                viewModel.revertManualHolidayOverride(date)
            }
        )
    }

    if (showManualImportSheet) {
        ManualBatchImportSheet(
            onDismiss = { showManualImportSheet = false },
            onImportText = { text ->
                viewModel.importManualScheduleText(text) { success ->
                    if (success) showManualImportSheet = false
                }
            }
        )
    }
}
