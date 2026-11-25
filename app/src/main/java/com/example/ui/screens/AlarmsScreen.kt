package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Undo
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.ChinaDayType
import com.example.data.model.HolidayRuleMode
import com.example.data.model.ResolvedDayInfo
import com.example.data.model.SimpleDate
import com.example.ui.components.DayTypePill
import com.example.ui.components.GroupIconAvatar
import com.example.ui.components.OneUiCard
import com.example.ui.components.OneUiSectionHeader
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.LocalOneUiColors
import com.example.ui.theme.OutfitDisplayFamily
import com.example.ui.viewmodel.AlarmGroupUiModel
import com.example.ui.viewmodel.AlarmItemUiModel
import com.example.ui.viewmodel.HolidayAlarmUiState

@Composable
fun AlarmsScreen(
    uiState: HolidayAlarmUiState,
    canScheduleExactAlarms: Boolean,
    onOpenExactAlarmSettings: () -> Unit,
    onSyncHolidays: () -> Unit,
    onAddAlarm: (Long?) -> Unit,
    onEditAlarm: (AlarmEntity) -> Unit,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onToggleSkipNext: (AlarmItemUiModel) -> Unit,
    onAddGroup: () -> Unit,
    onEditGroup: (AlarmGroupEntity) -> Unit,
    onToggleGroup: (Long, Boolean) -> Unit,
    onToggleSkipNextGroup: (AlarmGroupUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    val expandedGroups = remember { mutableStateMapOf<Long, Boolean>() }
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 148.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "oneui_hero") {
            OneUiHeroBanner(
                uiState = uiState,
                expanded = uiState.preferences.oneUiExpandedHeader,
                onSyncHolidays = onSyncHolidays
            )
        }

        if (!canScheduleExactAlarms) {
            item(key = "exact_alarm_warning") {
                OneUiCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = oneUi.lieuContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            tint = oneUi.lieuAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = s.exactAlarmWarning,
                            style = MaterialTheme.typography.titleSmall,
                            color = oneUi.lieuAccent,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onOpenExactAlarmSettings) {
                            Text(s.enableNow)
                        }
                    }
                }
            }
        }

        item(key = "groups_header") {
            OneUiSectionHeader(
                title = s.alarmGroupsHeader,
                trailing = {
                    TextButton(
                        onClick = onAddGroup,
                        modifier = Modifier.testTag("add_group_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CreateNewFolder,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s.newGroup)
                    }
                }
            )
        }

        if (uiState.groups.isEmpty() && uiState.ungroupedAlarms.isEmpty()) {
            item(key = "empty_state") {
                OneUiCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Alarm,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(s.noAlarmsYet, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        } else {
            items(uiState.groups, key = { "group_${it.group.id}" }) { groupModel ->
                val isExpanded = expandedGroups[groupModel.group.id] ?: true
                AlarmGroupCard(
                    model = groupModel,
                    todayInfo = uiState.todayInfo,
                    tomorrowInfo = uiState.tomorrowInfo,
                    isExpanded = isExpanded,
                    onToggleExpand = { expandedGroups[groupModel.group.id] = !isExpanded },
                    onToggleGroup = { enabled -> onToggleGroup(groupModel.group.id, enabled) },
                    onToggleSkipNextGroup = { onToggleSkipNextGroup(groupModel) },
                    onEditGroup = { onEditGroup(groupModel.group) },
                    onAddAlarmToGroup = { onAddAlarm(groupModel.group.id) },
                    onEditAlarm = onEditAlarm,
                    onToggleAlarm = onToggleAlarm,
                    onToggleSkipNext = onToggleSkipNext
                )
            }
        }

        if (uiState.ungroupedAlarms.isNotEmpty()) {
            item(key = "ungrouped_header") {
                OneUiSectionHeader(title = s.ungroupedAlarmsHeader)
            }

            items(uiState.ungroupedAlarms, key = { "alarm_${it.alarm.id}" }) { itemModel ->
                OneUiCard(modifier = Modifier.fillMaxWidth()) {
                    AlarmRowItem(
                        item = itemModel,
                        today = uiState.todayInfo.date,
                        groupPrimaryDate = null,
                        onEdit = { onEditAlarm(itemModel.alarm) },
                        onToggle = { enabled -> onToggleAlarm(itemModel.alarm.id, enabled) },
                        onToggleSkipNext = { onToggleSkipNext(itemModel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun OneUiHeroBanner(
    uiState: HolidayAlarmUiState,
    expanded: Boolean,
    onSyncHolidays: () -> Unit
) {
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current
    val nextOcc = uiState.nextGlobalOccurrence
    val today = uiState.todayInfo.date
    val heroTint = MaterialTheme.colorScheme.primaryContainer.copy(alpha = if (oneUi.isDark) 0.55f else 0.72f)
    val heroBase = MaterialTheme.colorScheme.surface
    val heroGradient = Brush.linearGradient(
        colors = listOf(heroTint, heroBase, heroBase)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, oneUi.cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroGradient)
                .padding(
                    horizontal = 20.dp,
                    vertical = if (expanded) 22.dp else 16.dp
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.appTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(enabled = !uiState.isSyncing, onClick = onSyncHolidays)
                        .testTag("hero_sync_button"),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.CloudSync,
                                contentDescription = s.syncHolidays,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (uiState.isSyncing) s.syncing else s.syncHolidays,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(2.dp))
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = uiState.nextCountdownText,
                    style = if (expanded) MaterialTheme.typography.headlineLarge
                    else MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (nextOcc != null) {
                    val relDay = s.formatRelativeDay(today, nextOcc.triggerDate)
                    val specialTag = when (nextOcc.dayInfo.dayType) {
                        ChinaDayType.LIEU_WORKDAY -> " · ${s.lieuWorkdayTag}"
                        ChinaDayType.STATUTORY_HOLIDAY -> " · ${s.localizeHolidayLabel(nextOcc.dayInfo.label, nextOcc.dayInfo.dayType)}"
                        else -> ""
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = when (nextOcc.dayInfo.dayType) {
                                ChinaDayType.LIEU_WORKDAY -> oneUi.lieuAccent
                                ChinaDayType.STATUTORY_HOLIDAY -> oneUi.holidayAccent
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "$relDay ${nextOcc.alarm.formattedTime()}$specialTag · ${nextOcc.alarm.label}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            val showTomorrowPill = uiState.tomorrowInfo.dayType != uiState.todayInfo.dayType ||
                uiState.tomorrowInfo.dayType == ChinaDayType.STATUTORY_HOLIDAY ||
                uiState.tomorrowInfo.dayType == ChinaDayType.LIEU_WORKDAY

            val todayLocalizedLabel = s.localizeHolidayLabel(uiState.todayInfo.label, uiState.todayInfo.dayType)
            val tomorrowLocalizedLabel = s.localizeHolidayLabel(uiState.tomorrowInfo.label, uiState.tomorrowInfo.dayType)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DayTypePill(
                    dayType = uiState.todayInfo.dayType,
                    label = "${s.formatShortDate(today)} ${s.formatWeekdayShort(today)} · $todayLocalizedLabel"
                )
                if (showTomorrowPill) {
                    DayTypePill(
                        dayType = uiState.tomorrowInfo.dayType,
                        label = s.heroTomorrowPrefix(tomorrowLocalizedLabel)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlarmGroupCard(
    model: AlarmGroupUiModel,
    todayInfo: ResolvedDayInfo,
    tomorrowInfo: ResolvedDayInfo,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleGroup: (Boolean) -> Unit,
    onToggleSkipNextGroup: () -> Unit,
    onEditGroup: () -> Unit,
    onAddAlarmToGroup: () -> Unit,
    onEditAlarm: (AlarmEntity) -> Unit,
    onToggleAlarm: (Long, Boolean) -> Unit,
    onToggleSkipNext: (AlarmItemUiModel) -> Unit
) {
    val group = model.group
    val accent = Color(group.accentColorHex)
    val haptics = LocalHapticFeedback.current
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current
    val today = todayInfo.date
    val tomorrow = tomorrowInfo.date

    val isGroupSkipped = model.activeSkippedDateLabel != null
    val canSkipGroup = group.isEnabled &&
        (isGroupSkipped || model.alarms.any { it.nextOccurrence != null })

    val groupPrimaryDate: SimpleDate? = when {
        !group.isEnabled || isGroupSkipped -> null
        model.remainingTodayCount > 0 -> today
        model.activeTomorrowCount > 0 -> tomorrow
        else -> null
    }

    OneUiCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alarm_group_card_${group.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggleExpand)
                .padding(start = 18.dp, end = 12.dp, top = 14.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GroupIconAvatar(iconKey = group.iconKey, accentColor = accent)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accent.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = s.ruleModeShortTag(group.ruleModeEnum),
                            style = MaterialTheme.typography.labelSmall,
                            color = accent,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                if (group.isEnabled && isGroupSkipped) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onToggleSkipNextGroup()
                            }
                            .testTag("group_skip_next_${group.id}"),
                        shape = RoundedCornerShape(50),
                        color = oneUi.holidayContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Undo,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = oneUi.holidayAccent
                            )
                            Text(
                                text = s.skippedUndoLabel(model.activeSkippedDateLabel!!),
                                style = MaterialTheme.typography.labelSmall,
                                color = oneUi.holidayAccent
                            )
                        }
                    }
                } else {
                    val statusText = when {
                        !group.isEnabled -> s.groupDisabled
                        model.alarms.isEmpty() -> s.noAlarmsYet
                        model.remainingTodayCount > 0 -> {
                            if (todayInfo.dayType == ChinaDayType.LIEU_WORKDAY) {
                                s.todayLieuPendingCount(model.remainingTodayCount)
                            } else {
                                s.todayPendingCount(model.remainingTodayCount)
                            }
                        }
                        model.activeTomorrowCount > 0 && model.autoPausedTomorrowCount == 0 -> {
                            if (tomorrowInfo.dayType == ChinaDayType.LIEU_WORKDAY) {
                                s.tomorrowLieuPendingCount(model.activeTomorrowCount)
                            } else {
                                s.tomorrowPendingCount(model.activeTomorrowCount)
                            }
                        }
                        model.activeTomorrowCount == 0 && model.autoPausedTomorrowCount > 0 -> {
                            if (tomorrowInfo.dayType == ChinaDayType.STATUTORY_HOLIDAY) {
                                s.tomorrowHolidaySkipped(
                                    s.localizeHolidayLabel(tomorrowInfo.label, tomorrowInfo.dayType)
                                )
                            } else {
                                s.tomorrowAutoSkipped
                            }
                        }
                        model.activeTomorrowCount > 0 ->
                            s.tomorrowMixedStatus(model.activeTomorrowCount, model.autoPausedTomorrowCount)
                        else -> s.noPendingAlarms
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            !group.isEnabled -> MaterialTheme.colorScheme.onSurfaceVariant
                            model.remainingTodayCount > 0 && todayInfo.dayType == ChinaDayType.LIEU_WORKDAY ->
                                oneUi.lieuAccent
                            model.remainingTodayCount > 0 -> oneUi.activeAccent
                            model.activeTomorrowCount > 0 && tomorrowInfo.dayType == ChinaDayType.LIEU_WORKDAY ->
                                oneUi.lieuAccent
                            model.activeTomorrowCount > 0 -> oneUi.activeAccent
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (canSkipGroup && !isGroupSkipped) {
                IconButton(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onToggleSkipNextGroup()
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("group_skip_next_${group.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsOff,
                        contentDescription = s.skipNext,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            IconButton(
                onClick = onEditGroup,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = s.editGroupDesc,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(19.dp)
                )
            }

            Switch(
                checked = group.isEnabled,
                onCheckedChange = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onToggleGroup(it)
                },
                modifier = Modifier.testTag("group_switch_${group.id}")
            )
        }

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                model.alarms.forEach { itemModel ->
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                    )
                    AlarmRowItem(
                        item = itemModel,
                        today = today,
                        groupPrimaryDate = groupPrimaryDate,
                        onEdit = { onEditAlarm(itemModel.alarm) },
                        onToggle = { enabled -> onToggleAlarm(itemModel.alarm.id, enabled) },
                        onToggleSkipNext = { onToggleSkipNext(itemModel) }
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 18.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onAddAlarmToGroup) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(s.addAlarm)
                    }
                    IconButton(onClick = onToggleExpand) {
                        Icon(
                            imageVector = Icons.Rounded.ExpandLess,
                            contentDescription = s.collapseGroup,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (!isExpanded) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.expandGroupCount(model.alarms.size),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = s.expandGroup,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AlarmRowItem(
    item: AlarmItemUiModel,
    today: SimpleDate,
    groupPrimaryDate: SimpleDate?,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onToggleSkipNext: () -> Unit
) {
    val alarm = item.alarm
    val group = item.group
    val effectivelyOn = alarm.isEffectivelyEnabled(group)
    val haptics = LocalHapticFeedback.current
    val oneUi = LocalOneUiColors.current
    val s = LocalAppStrings.current
    val effectiveRule = alarm.effectiveRuleMode(group)
    val nextOcc = item.nextOccurrence
    val isIndividuallySkipped = item.activeSkippedDateLabel != null
    val isIndependentInGroup = group != null && !alarm.inheritGroupRule

    val ruleShortText = when (effectiveRule) {
        HolidayRuleMode.CUSTOM_SKIP_HOLIDAY ->
            s.formatCustomDaysMask(alarm.effectiveCustomDaysMask(group))
        else -> s.ruleModeShortTag(effectiveRule)
    }

    val subtitleInfo: Pair<String, Color>? = when {
        !effectivelyOn -> {
            if (group == null || isIndependentInGroup) {
                ruleShortText to MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                null
            }
        }
        nextOcc == null -> s.noSchedule to MaterialTheme.colorScheme.onSurfaceVariant
        else -> {
            val relDay = s.formatRelativeDay(today, nextOcc.triggerDate)
            val isLieu = nextOcc.dayInfo.dayType == ChinaDayType.LIEU_WORKDAY
            val isHoliday = nextOcc.dayInfo.dayType == ChinaDayType.STATUTORY_HOLIDAY
            val hasHolidaySkip = nextOcc.skippedReason != null &&
                !isIndividuallySkipped &&
                group?.skippedDateIso == null

            when {
                isLieu -> {
                    val prefix = if (isIndependentInGroup) s.independentPrefix else ""
                    "$prefix$relDay · ${s.lieuWorkdayTag}" to oneUi.lieuAccent
                }
                hasHolidaySkip -> {
                    val prefix = if (isIndependentInGroup) s.independentPrefix else ""
                    "$prefix$relDay${s.afterHolidaySuffix}" to oneUi.holidayAccent
                }
                isHoliday && effectiveRule == HolidayRuleMode.LEGAL_HOLIDAY_AND_WEEKEND -> {
                    val prefix = if (isIndependentInGroup) s.independentPrefix else ""
                    val holidayName = s.localizeHolidayLabel(nextOcc.dayInfo.label, nextOcc.dayInfo.dayType)
                    "$prefix$relDay · $holidayName" to oneUi.holidayAccent
                }
                group == null -> {
                    "$ruleShortText · $relDay" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                isIndependentInGroup -> {
                    "${s.independentPrefix}$ruleShortText · $relDay" to MaterialTheme.colorScheme.onSurfaceVariant
                }
                groupPrimaryDate == null || nextOcc.triggerDate != groupPrimaryDate -> {
                    relDay to MaterialTheme.colorScheme.onSurfaceVariant
                }
                else -> null
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
            .padding(start = 18.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)
            .alpha(if (effectivelyOn) 1f else 0.54f)
            .testTag("alarm_item_${alarm.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = alarm.formattedTime(),
                    fontFamily = OutfitDisplayFamily,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            if (effectivelyOn && isIndividuallySkipped) {
                Spacer(modifier = Modifier.height(5.dp))
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSkipNext()
                        }
                        .testTag("alarm_skip_next_${alarm.id}"),
                    shape = RoundedCornerShape(50),
                    color = oneUi.holidayContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Undo,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = oneUi.holidayAccent
                        )
                        Text(
                            text = s.skippedUndoLabel(item.activeSkippedDateLabel!!),
                            style = MaterialTheme.typography.labelSmall,
                            color = oneUi.holidayAccent
                        )
                    }
                }
            } else if (subtitleInfo != null) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitleInfo.first,
                    style = MaterialTheme.typography.bodySmall,
                    color = subtitleInfo.second,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (effectivelyOn && !isIndividuallySkipped && nextOcc != null) {
            IconButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleSkipNext()
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("alarm_skip_next_${alarm.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsOff,
                    contentDescription = s.skipNext,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(2.dp))

        Switch(
            checked = alarm.isEnabled,
            onCheckedChange = {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onToggle(it)
            },
            modifier = Modifier.testTag("alarm_switch_${alarm.id}")
        )
    }
}
