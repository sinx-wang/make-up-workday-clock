package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BeachAccess
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.WorkOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.data.model.ChinaDayType
import com.example.ui.theme.LocalOneUiColors

/**
 * Signature One UI 9 squircle card container with subtle AMOLED-friendly stroke.
 */
@Composable
fun OneUiCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 26.dp,
    containerColor: Color = LocalOneUiColors.current.cardSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val oneUi = LocalOneUiColors.current
    val shape = RoundedCornerShape(cornerRadius)
    val baseModifier = if (onClick != null) {
        modifier
            .clip(shape)
            .clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, oneUi.cardBorder)
    ) {
        Column(content = content)
    }
}

@Composable
fun DayTypePill(
    dayType: ChinaDayType,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val oneUi = LocalOneUiColors.current
    val s = com.example.ui.i18n.LocalAppStrings.current
    val (bg, fg) = when (dayType) {
        ChinaDayType.STATUTORY_HOLIDAY -> oneUi.holidayContainer to oneUi.holidayAccent
        ChinaDayType.LIEU_WORKDAY -> oneUi.lieuContainer to oneUi.lieuAccent
        ChinaDayType.WORKDAY -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        ChinaDayType.WEEKEND -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = bg
    ) {
        Text(
            text = label?.takeIf { it.isNotBlank() } ?: s.dayTypeDefaultPill(dayType),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

fun groupIconForKey(key: String): ImageVector = when (key) {
    "work" -> Icons.Rounded.WorkOutline
    "holiday" -> Icons.Rounded.BeachAccess
    "fitness" -> Icons.Rounded.FitnessCenter
    "family" -> Icons.Rounded.Home
    "study" -> Icons.Rounded.School
    else -> Icons.Rounded.AutoAwesome
}

@Composable
fun GroupIconAvatar(
    iconKey: String,
    accentColor: Color,
    size: Dp = 42.dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(accentColor.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = groupIconForKey(iconKey),
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(size * 0.54f)
        )
    }
}

@Composable
fun OneUiSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

@Composable
fun WeekdayMaskSelector(
    mask: Int,
    onMaskChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = com.example.ui.i18n.LocalAppStrings.current
    val labels = s.weekdayInitials
    val haptics = LocalHapticFeedback.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        labels.forEachIndexed { index, dayChar ->
            val selected = (mask and (1 shl index)) != 0
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                    .clickable {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onMaskChange(mask xor (1 shl index))
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dayChar,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
