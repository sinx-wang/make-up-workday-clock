package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ChinaDayType
import com.example.data.model.ResolvedDayInfo
import com.example.data.model.SimpleDate
import com.example.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDayOverrideSheet(
    dayInfo: ResolvedDayInfo,
    onDismiss: () -> Unit,
    onApplyOverride: (SimpleDate, ChinaDayType, String) -> Unit,
    onApplyRange: (SimpleDate, SimpleDate, ChinaDayType, String) -> Unit,
    onRevertOfficial: (SimpleDate) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val s = LocalAppStrings.current
    val initialLocalizedLabel = remember(dayInfo, s.language) {
        s.localizeHolidayLabel(dayInfo.label, dayInfo.dayType)
    }
    var selectedType by remember { mutableStateOf(dayInfo.dayType) }
    var label by remember { mutableStateOf(initialLocalizedLabel) }
    var spanDays by remember { mutableIntStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = s.formatFullDate(dayInfo.date),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                DayTypePill(
                    dayType = dayInfo.dayType,
                    label = initialLocalizedLabel
                )
            }

            listOf(
                ChinaDayType.STATUTORY_HOLIDAY,
                ChinaDayType.LIEU_WORKDAY,
                ChinaDayType.WORKDAY,
                ChinaDayType.WEEKEND
            ).forEach { type ->
                val selected = selectedType == type
                val typeLabel = s.dayTypeDisplayName(type)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable {
                            selectedType = type
                            if (label.isBlank() || label == initialLocalizedLabel) {
                                label = typeLabel
                            }
                        },
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selected, onClick = { selectedType = type })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(typeLabel, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text(s.noteLabel) },
                placeholder = { Text(s.notePlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            )

            // Quick multi-day span
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.applyMultiDayTitle, style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1, 2, 3, 5, 7).forEach { count ->
                        FilterChip(
                            selected = spanDays == count,
                            onClick = { spanDays = count },
                            label = { Text(if (count == 1) s.onlyThisDay else s.spanDaysChip(count)) }
                        )
                    }
                }
            }

            if (dayInfo.isUserOverride) {
                OutlinedButton(
                    onClick = {
                        onRevertOfficial(dayInfo.date)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Rounded.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(s.revertOfficialDefault)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(s.cancel)
                }
                Button(
                    onClick = {
                        if (spanDays == 1) {
                            onApplyOverride(dayInfo.date, selectedType, label)
                        } else {
                            onApplyRange(
                                dayInfo.date,
                                dayInfo.date.plusDays(spanDays - 1),
                                selectedType,
                                label
                            )
                        }
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.6f)
                        .height(50.dp)
                        .testTag("apply_holiday_override_button"),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(s.saveAndReschedule)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualBatchImportSheet(
    onDismiss: () -> Unit,
    onImportText: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val s = LocalAppStrings.current
    var inputText by remember(s.language) { mutableStateOf(s.manualImportTemplate) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = s.manualImportTitle,
                style = MaterialTheme.typography.headlineSmall
            )
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                minLines = 6,
                maxLines = 10,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("manual_import_input")
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(s.cancel)
                }
                Button(
                    onClick = { onImportText(inputText) },
                    modifier = Modifier
                        .weight(1.5f)
                        .height(50.dp)
                        .testTag("confirm_manual_import_button"),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(s.parseAndUpdate)
                }
            }
        }
    }
}
