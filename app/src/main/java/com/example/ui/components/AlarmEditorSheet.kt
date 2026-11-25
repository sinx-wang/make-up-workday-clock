package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlarmEntity
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.HolidayRuleMode
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.theme.OutfitDisplayFamily
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditorSheet(
    initialAlarm: AlarmEntity?,
    defaultGroupId: Long?,
    groups: List<AlarmGroupEntity>,
    onDismiss: () -> Unit,
    onSave: (AlarmEntity) -> Unit,
    onDelete: ((AlarmEntity) -> Unit)? = null,
    onTestRing: (AlarmEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current
    val s = LocalAppStrings.current
    val minuteFocusRequester = remember { FocusRequester() }

    var hour by remember { mutableIntStateOf(initialAlarm?.hour ?: 7) }
    var minute by remember { mutableIntStateOf(initialAlarm?.minute ?: 30) }
    var hourField by remember {
        mutableStateOf(TextFieldValue(String.format(Locale.US, "%02d", initialAlarm?.hour ?: 7)))
    }
    var minuteField by remember {
        mutableStateOf(TextFieldValue(String.format(Locale.US, "%02d", initialAlarm?.minute ?: 30)))
    }
    var hourError by remember { mutableStateOf<String?>(null) }
    var minuteError by remember { mutableStateOf<String?>(null) }

    var label by remember { mutableStateOf(initialAlarm?.label ?: "") }
    var selectedGroupId by remember { mutableStateOf(initialAlarm?.groupId ?: defaultGroupId) }
    var inheritGroupRule by remember { mutableStateOf(initialAlarm?.inheritGroupRule ?: (selectedGroupId != null)) }
    var ruleMode by remember {
        mutableStateOf(
            HolidayRuleMode.fromName(initialAlarm?.ruleMode ?: HolidayRuleMode.LEGAL_WORKDAY.name)
        )
    }
    var customDaysMask by remember { mutableIntStateOf(initialAlarm?.customDaysMask ?: 0b0011111) }
    var includeLieuWorkdays by remember { mutableStateOf(initialAlarm?.includeLieuWorkdays ?: true) }
    var vibrate by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }
    var gradualVolume by remember { mutableStateOf(initialAlarm?.gradualVolume ?: true) }
    var snoozeMinutes by remember { mutableIntStateOf(initialAlarm?.snoozeMinutes ?: 5) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    var ringtoneUri by remember { mutableStateOf(initialAlarm?.ringtoneUri) }
    var ringtoneTitle by remember {
        mutableStateOf(
            if (initialAlarm?.ringtoneUri == null) s.defaultRingtoneTitle
            else initialAlarm.ringtoneTitle.ifBlank { s.defaultRingtoneTitle }
        )
    }

    val selectedGroup = groups.firstOrNull { it.id == selectedGroupId }
    val timeValidationError = hourError ?: minuteError

    fun syncTimeFields(newHour: Int, newMinute: Int) {
        hour = newHour.coerceIn(0, 23)
        minute = newMinute.coerceIn(0, 59)
        hourField = TextFieldValue(String.format(Locale.US, "%02d", hour))
        minuteField = TextFieldValue(String.format(Locale.US, "%02d", minute))
        hourError = null
        minuteError = null
    }

    fun validateTimeBeforeSave(): Boolean {
        val parsedH = hourField.text.toIntOrNull()
        val parsedM = minuteField.text.toIntOrNull()
        var valid = true
        if (parsedH == null || parsedH !in 0..23) {
            hourError = s.errorHourSaveRange
            valid = false
        } else {
            hour = parsedH
            hourError = null
        }
        if (parsedM == null || parsedM !in 0..59) {
            minuteError = s.errorMinuteSaveRange
            valid = false
        } else {
            minute = parsedM
            minuteError = null
        }
        return valid
    }

    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            if (uri != null) {
                ringtoneUri = uri.toString()
                val ringtone = RingtoneManager.getRingtone(context, uri)
                ringtoneTitle = ringtone?.getTitle(context)?.takeIf { it.isNotBlank() } ?: s.customRingtoneTitle
            } else {
                ringtoneUri = null
                ringtoneTitle = s.defaultRingtoneTitle
            }
        }
    }

    fun buildCurrentEntity(): AlarmEntity = AlarmEntity(
        id = initialAlarm?.id ?: 0L,
        groupId = selectedGroupId,
        hour = hour,
        minute = minute,
        label = label.trim().ifBlank { s.defaultAlarmLabel },
        isEnabled = initialAlarm?.isEnabled ?: true,
        inheritGroupRule = selectedGroupId != null && inheritGroupRule,
        ruleMode = ruleMode.name,
        customDaysMask = customDaysMask,
        includeLieuWorkdays = includeLieuWorkdays,
        vibrate = vibrate,
        gradualVolume = gradualVolume,
        snoozeMinutes = snoozeMinutes,
        ringtoneUri = ringtoneUri,
        ringtoneTitle = ringtoneTitle,
        skippedDateIso = null
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        // Resolve FocusManager / KeyboardController / View INSIDE ModalBottomSheet's own dialog window
        val sheetFocusManager = LocalFocusManager.current
        val sheetKeyboardController = LocalSoftwareKeyboardController.current
        val sheetView = LocalView.current

        fun dismissInput() {
            sheetKeyboardController?.hide()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(sheetView.windowToken, 0)
            sheetFocusManager.clearFocus(force = true)
            sheetView.clearFocus()
        }

        val isImeVisible = WindowInsets.ime.getBottom(density) > 0
        var wasImeVisible by remember { mutableStateOf(false) }
        LaunchedEffect(isImeVisible) {
            if (wasImeVisible && !isImeVisible && !showCustomSnoozeDialog) {
                sheetFocusManager.clearFocus(force = true)
                sheetView.clearFocus()
            }
            wasImeVisible = isImeVisible
        }

        DisposableEffect(sheetView) {
            var lastNativeImeVisible = ViewCompat.getRootWindowInsets(sheetView)
                ?.isVisible(WindowInsetsCompat.Type.ime()) == true
            val listener = ViewTreeObserver.OnGlobalLayoutListener {
                val nowVisible = ViewCompat.getRootWindowInsets(sheetView)
                    ?.isVisible(WindowInsetsCompat.Type.ime()) == true
                if (lastNativeImeVisible && !nowVisible && !showCustomSnoozeDialog) {
                    sheetFocusManager.clearFocus(force = true)
                    sheetView.clearFocus()
                }
                lastNativeImeVisible = nowVisible
            }
            sheetView.viewTreeObserver.addOnGlobalLayoutListener(listener)
            onDispose {
                if (sheetView.viewTreeObserver.isAlive) {
                    sheetView.viewTreeObserver.removeOnGlobalLayoutListener(listener)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    dismissInput()
                }
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .imePadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialAlarm == null) s.newAlarm else s.editAlarm,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (initialAlarm != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(initialAlarm)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_alarm_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = s.deleteAlarm,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // One UI Large Time Picker Stepper + Direct Numeric Input Card
            OneUiCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TimeUnitStepper(
                            fieldValue = hourField,
                            label = s.hourUnit,
                            imeAction = ImeAction.Done,
                            isError = hourError != null,
                            onValueChange = { incoming ->
                                val digits = incoming.text.filter { it.isDigit() }.take(2)
                                val textChanged = digits != hourField.text
                                hourField = if (digits == incoming.text) {
                                    incoming
                                } else {
                                    incoming.copy(
                                        text = digits,
                                        selection = TextRange(incoming.selection.end.coerceAtMost(digits.length))
                                    )
                                }
                                if (!textChanged) return@TimeUnitStepper
                                val parsed = digits.toIntOrNull()
                                when {
                                    digits.isEmpty() -> hourError = null
                                    parsed != null && parsed in 0..23 -> {
                                        hour = parsed
                                        hourError = null
                                        if (digits.length == 2) {
                                            minuteFocusRequester.requestFocus()
                                        }
                                    }
                                    else -> {
                                        hourError = s.errorHourRange
                                    }
                                }
                            },
                            onFocusGained = {
                                hourField = hourField.copy(
                                    selection = TextRange(0, hourField.text.length)
                                )
                            },
                            onFocusLost = {
                                val parsed = hourField.text.toIntOrNull()
                                if (parsed != null && parsed in 0..23) {
                                    hour = parsed
                                    hourError = null
                                    hourField = TextFieldValue(String.format(Locale.US, "%02d", hour))
                                } else if (hourField.text.isEmpty()) {
                                    hourError = null
                                    hourField = TextFieldValue(String.format(Locale.US, "%02d", hour))
                                }
                            },
                            onImeAction = { dismissInput() },
                            onDecrement = {
                                dismissInput()
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                syncTimeFields((hour + 23) % 24, minute)
                            },
                            onIncrement = {
                                dismissInput()
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                syncTimeFields((hour + 1) % 24, minute)
                            },
                            inputTag = "alarm_hour_input"
                        )
                        Text(
                            text = ":",
                            fontFamily = OutfitDisplayFamily,
                            fontSize = 46.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TimeUnitStepper(
                            fieldValue = minuteField,
                            label = s.minuteUnit,
                            imeAction = ImeAction.Done,
                            isError = minuteError != null,
                            inputModifier = Modifier.focusRequester(minuteFocusRequester),
                            onValueChange = { incoming ->
                                val digits = incoming.text.filter { it.isDigit() }.take(2)
                                val textChanged = digits != minuteField.text
                                minuteField = if (digits == incoming.text) {
                                    incoming
                                } else {
                                    incoming.copy(
                                        text = digits,
                                        selection = TextRange(incoming.selection.end.coerceAtMost(digits.length))
                                    )
                                }
                                if (!textChanged) return@TimeUnitStepper
                                val parsed = digits.toIntOrNull()
                                when {
                                    digits.isEmpty() -> minuteError = null
                                    parsed != null && parsed in 0..59 -> {
                                        minute = parsed
                                        minuteError = null
                                        if (digits.length == 2) {
                                            dismissInput()
                                        }
                                    }
                                    else -> {
                                        minuteError = s.errorMinuteRange
                                    }
                                }
                            },
                            onFocusGained = {
                                minuteField = minuteField.copy(
                                    selection = TextRange(0, minuteField.text.length)
                                )
                            },
                            onFocusLost = {
                                val parsed = minuteField.text.toIntOrNull()
                                if (parsed != null && parsed in 0..59) {
                                    minute = parsed
                                    minuteError = null
                                    minuteField = TextFieldValue(String.format(Locale.US, "%02d", minute))
                                } else if (minuteField.text.isEmpty()) {
                                    minuteError = null
                                    minuteField = TextFieldValue(String.format(Locale.US, "%02d", minute))
                                }
                            },
                            onImeAction = { dismissInput() },
                            onDecrement = {
                                dismissInput()
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                syncTimeFields(hour, (minute + 59) % 60)
                            },
                            onIncrement = {
                                dismissInput()
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                syncTimeFields(hour, (minute + 1) % 60)
                            },
                            inputTag = "alarm_minute_input"
                        )
                    }

                    if (!timeValidationError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = timeValidationError!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    // Quick One UI preset times
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(6 to 30, 7 to 0, 7 to 30, 8 to 0, 9 to 0, 22 to 30).forEach { (h, m) ->
                            val isMatch = hour == h && minute == m
                            FilterChip(
                                selected = isMatch,
                                onClick = {
                                    dismissInput()
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    syncTimeFields(h, m)
                                },
                                label = {
                                    Text(String.format(Locale.US, "%02d:%02d", h, m))
                                }
                            )
                        }
                    }
                }
            }

            // Label Input
            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(s.alarmNameLabel) },
                placeholder = { Text(s.alarmNamePlaceholder) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { dismissInput() }),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("alarm_label_input")
            )

            // Group Assignment
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = s.alarmGroupSection,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedGroupId == null,
                        onClick = {
                            selectedGroupId = null
                            inheritGroupRule = false
                        },
                        label = { Text(s.standaloneAlarm) }
                    )
                    groups.forEach { grp ->
                        FilterChip(
                            selected = selectedGroupId == grp.id,
                            onClick = {
                                selectedGroupId = grp.id
                                inheritGroupRule = true
                            },
                            label = { Text(grp.name) }
                        )
                    }
                }
            }

            // Inherit group rule toggle
            if (selectedGroup != null) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = s.followGroupRule(
                                    selectedGroup.name,
                                    s.ruleModeShortTag(selectedGroup.ruleModeEnum)
                                ),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        Switch(
                            checked = inheritGroupRule,
                            onCheckedChange = { inheritGroupRule = it }
                        )
                    }
                }
            }

            // Holiday Rule Selection (shown if ungrouped or overriding group rule)
            if (selectedGroup == null || !inheritGroupRule) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = s.repeatAndHolidayRules,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HolidayRuleMode.entries.forEach { mode ->
                        val selected = ruleMode == mode
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    ruleMode = mode
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selected,
                                    onClick = { ruleMode = mode }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = s.ruleModeTitle(mode),
                                    style = MaterialTheme.typography.titleSmall,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    if (ruleMode == HolidayRuleMode.CUSTOM_SKIP_HOLIDAY) {
                        Spacer(modifier = Modifier.height(4.dp))
                        WeekdayMaskSelector(
                            mask = customDaysMask,
                            onMaskChange = { customDaysMask = it }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Sound, Vibration, Gradual Volume, Snooze
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, s.selectRingtoneTitle)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                                ringtoneUri?.let {
                                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(it))
                                }
                            }
                            ringtonePickerLauncher.launch(intent)
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(s.alarmRingtone, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = ringtoneTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = s.change,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.gradualVolume, style = MaterialTheme.typography.titleSmall)
                    Switch(checked = gradualVolume, onCheckedChange = { gradualVolume = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.vibrateReminder, style = MaterialTheme.typography.titleSmall)
                    Switch(checked = vibrate, onCheckedChange = { vibrate = it })
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(s.snoozeInterval, style = MaterialTheme.typography.titleSmall)
                    val presets = listOf(5, 10, 15)
                    val isCustomSnooze = snoozeMinutes !in presets
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { mins ->
                            FilterChip(
                                selected = snoozeMinutes == mins,
                                onClick = {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    snoozeMinutes = mins
                                },
                                label = { Text(s.minutesLabel(mins)) }
                            )
                        }
                        FilterChip(
                            selected = isCustomSnooze,
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                showCustomSnoozeDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                            },
                            label = {
                                Text(if (isCustomSnooze) s.minutesLabel(snoozeMinutes) else s.customOption)
                            },
                            modifier = Modifier.testTag("custom_snooze_chip")
                        )
                    }
                }
            }

            // Utility actions: Test Ring & Export to Samsung Clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onTestRing(buildCurrentEntity()) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("test_ring_button"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(s.testRing)
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                            putExtra(AlarmClock.EXTRA_HOUR, hour)
                            putExtra(AlarmClock.EXTRA_MINUTES, minute)
                            putExtra(AlarmClock.EXTRA_MESSAGE, label.ifBlank { s.appTitle })
                            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                        }
                        runCatching { context.startActivity(intent) }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WatchLater,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(s.syncSystemClock)
                }
            }

            // Primary Save / Cancel Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                ) {
                    Text(s.cancel)
                }
                Button(
                    onClick = {
                        if (validateTimeBeforeSave()) {
                            dismissInput()
                            onSave(buildCurrentEntity())
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .weight(1.6f)
                        .height(52.dp)
                        .testTag("save_alarm_button"),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(s.saveAndSchedule, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }

    if (showCustomSnoozeDialog) {
        CustomSnoozeDialog(
            initialMinutes = snoozeMinutes,
            onDismiss = { showCustomSnoozeDialog = false },
            onConfirm = { mins ->
                snoozeMinutes = mins
                showCustomSnoozeDialog = false
            }
        )
    }
}

@Composable
private fun CustomSnoozeDialog(
    initialMinutes: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val s = LocalAppStrings.current
    val initialText = initialMinutes.toString()
    var input by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(0, initialText.length)
            )
        )
    }
    var errorText by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    fun submit(hideKeyboard: () -> Unit) {
        val parsed = input.text.trim().toIntOrNull()
        if (parsed == null || parsed !in 1..60) {
            errorText = s.customSnoozeErrorSubmit
        } else {
            hideKeyboard()
            onConfirm(parsed)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(28.dp),
        title = {
            Text(s.customSnoozeTitle, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            val dialogKeyboard = LocalSoftwareKeyboardController.current
            LaunchedEffect(Unit) {
                runCatching { focusRequester.requestFocus() }
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { incoming ->
                        val digits = incoming.text.filter { it.isDigit() }.take(2)
                        input = if (digits == incoming.text) {
                            incoming
                        } else {
                            incoming.copy(
                                text = digits,
                                selection = TextRange(incoming.selection.end.coerceAtMost(digits.length))
                            )
                        }
                        val parsed = digits.toIntOrNull()
                        errorText = when {
                            digits.isEmpty() -> null
                            parsed == null || parsed !in 1..60 -> s.customSnoozeErrorRange
                            else -> null
                        }
                    },
                    label = { Text(s.customSnoozeHint) },
                    suffix = { Text(s.minuteUnitSuffix) },
                    isError = errorText != null,
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submit { dialogKeyboard?.hide() } },
                        onNext = { submit { dialogKeyboard?.hide() } },
                        onGo = { submit { dialogKeyboard?.hide() } }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("custom_snooze_input")
                )
                if (errorText != null) {
                    Text(
                        text = errorText!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            val dialogKeyboard = LocalSoftwareKeyboardController.current
            Button(
                onClick = { submit { dialogKeyboard?.hide() } },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("confirm_custom_snooze_button")
            ) {
                Text(s.confirm)
            }
        },
        dismissButton = {
            val dialogKeyboard = LocalSoftwareKeyboardController.current
            TextButton(
                onClick = {
                    dialogKeyboard?.hide()
                    onDismiss()
                }
            ) {
                Text(s.cancel)
            }
        }
    )
}

@Composable
private fun TimeUnitStepper(
    fieldValue: TextFieldValue,
    label: String,
    imeAction: ImeAction,
    isError: Boolean,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusGained: () -> Unit,
    onFocusLost: () -> Unit,
    onImeAction: () -> Unit,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    inputTag: String,
    inputModifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    var isFocused by remember { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onIncrement,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        ) {
            Icon(Icons.Rounded.Add, contentDescription = s.increaseUnit(label))
        }
        Spacer(modifier = Modifier.height(8.dp))
        BasicTextField(
            value = fieldValue,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = OutfitDisplayFamily,
                fontSize = 48.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            cursorBrush = SolidColor(
                if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onDone = { onImeAction() },
                onNext = { onImeAction() },
                onGo = { onImeAction() },
                onSend = { onImeAction() },
                onSearch = { onImeAction() }
            ),
            modifier = inputModifier
                .width(96.dp)
                .height(68.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp &&
                        (event.key == Key.Enter || event.key == Key.NumPadEnter)
                    ) {
                        onImeAction()
                        true
                    } else {
                        false
                    }
                }
                .onFocusChanged { state ->
                    if (state.isFocused && !isFocused) {
                        onFocusGained()
                    } else if (!state.isFocused && isFocused) {
                        onFocusLost()
                    }
                    isFocused = state.isFocused
                }
                .testTag(inputTag),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .height(68.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            when {
                                isError -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                                isFocused -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else -> MaterialTheme.colorScheme.surface.copy(alpha = 0.72f)
                            }
                        )
                        .border(
                            width = if (isFocused || isError) 2.dp else 1.dp,
                            color = when {
                                isError -> MaterialTheme.colorScheme.error
                                isFocused -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.outlineVariant
                            },
                            shape = RoundedCornerShape(18.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    innerTextField()
                }
            }
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        IconButton(
            onClick = onDecrement,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
        ) {
            Icon(Icons.Rounded.Remove, contentDescription = s.decreaseUnit(label))
        }
    }
}
