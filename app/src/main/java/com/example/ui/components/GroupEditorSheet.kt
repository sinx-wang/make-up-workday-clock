package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.AlarmGroupEntity
import com.example.data.model.HolidayRuleMode
import com.example.ui.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupEditorSheet(
    initialGroup: AlarmGroupEntity?,
    onDismiss: () -> Unit,
    onSave: (AlarmGroupEntity) -> Unit,
    onDelete: ((AlarmGroupEntity) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val s = LocalAppStrings.current

    var name by remember { mutableStateOf(initialGroup?.name ?: "") }
    var description by remember { mutableStateOf(initialGroup?.description ?: "") }
    var iconKey by remember { mutableStateOf(initialGroup?.iconKey ?: "work") }
    var accentColorHex by remember { mutableLongStateOf(initialGroup?.accentColorHex ?: 0xFF387AFF) }
    var ruleMode by remember {
        mutableStateOf(
            HolidayRuleMode.fromName(initialGroup?.defaultRuleMode ?: HolidayRuleMode.LEGAL_WORKDAY.name)
        )
    }
    var customDaysMask by remember { mutableIntStateOf(initialGroup?.customDaysMask ?: 0b0011111) }

    val iconKeys = listOf("work", "holiday", "fitness", "family", "study", "custom")
    val accentOptions = listOf(
        0xFF387AFFL,
        0xFFFF5A5FL,
        0xFF10B981L,
        0xFFFF9F1CL,
        0xFF7C5CFFL,
        0xFF00A8E8L
    )

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (initialGroup == null) s.newGroupTitle else s.editGroupTitle,
                    style = MaterialTheme.typography.headlineSmall
                )
                if (initialGroup != null && onDelete != null) {
                    IconButton(
                        onClick = {
                            onDelete(initialGroup)
                            onDismiss()
                        },
                        modifier = Modifier.testTag("delete_group_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = s.deleteGroupDesc,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(s.groupNameLabel) },
                placeholder = { Text(s.groupNamePlaceholder) },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("group_name_input")
            )

            // Icon & Color Selector
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.iconAndAccent, style = MaterialTheme.typography.titleSmall)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    iconKeys.forEach { key ->
                        val selected = iconKey == key
                        val label = s.groupIconLabel(key)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { iconKey = key }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (selected) Color(accentColorHex).copy(alpha = 0.2f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (selected) 2.dp else 0.dp,
                                        color = if (selected) Color(accentColorHex) else Color.Transparent,
                                        shape = RoundedCornerShape(14.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = groupIconForKey(key),
                                    contentDescription = label,
                                    tint = if (selected) Color(accentColorHex)
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    accentOptions.forEach { hex ->
                        val selected = accentColorHex == hex
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .clickable { accentColorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = s.selectedColorDesc,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Group Default Holiday Rule
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(s.repeatAndHolidayRules, style = MaterialTheme.typography.titleSmall)
                HolidayRuleMode.entries.forEach { mode ->
                    val selected = ruleMode == mode
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .clickable { ruleMode = mode },
                        shape = RoundedCornerShape(18.dp),
                        color = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selected, onClick = { ruleMode = mode })
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
                        val entity = AlarmGroupEntity(
                            id = initialGroup?.id ?: 0L,
                            name = name.trim().ifBlank { s.defaultGroupLabel },
                            description = description.trim().ifBlank { s.ruleModeDescription(ruleMode) },
                            iconKey = iconKey,
                            accentColorHex = accentColorHex,
                            isEnabled = initialGroup?.isEnabled ?: true,
                            defaultRuleMode = ruleMode.name,
                            customDaysMask = customDaysMask,
                            includeLieuWorkdays = false,
                            skippedDateIso = initialGroup?.skippedDateIso,
                            sortOrder = initialGroup?.sortOrder ?: 0
                        )
                        onSave(entity)
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1.6f)
                        .height(52.dp)
                        .testTag("save_group_button"),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Text(s.saveGroup, style = MaterialTheme.typography.titleSmall)
                }
            }
        }
    }
}
