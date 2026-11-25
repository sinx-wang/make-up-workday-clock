package com.example.alarm

import android.app.KeyguardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlarmOff
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Snooze
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.HolidayAlarmApp
import com.example.data.local.AppPreferences
import com.example.data.local.ThemeMode
import com.example.data.model.ChinaDayType
import com.example.ui.i18n.LocalAppStrings
import com.example.ui.i18n.StringPacks
import com.example.ui.theme.HolidayAlarmTheme
import com.example.ui.theme.OutfitDisplayFamily

/**
 * Full-screen lockscreen-capable One UI 9 ringing screen for Galaxy S26 Ultra.
 */
class AlarmRingingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureShowOverLockscreen()

        val alarmId = intent.getLongExtra(AlarmConstants.EXTRA_ALARM_ID, -1L)
        val rawLabel = intent.getStringExtra(AlarmConstants.EXTRA_ALARM_LABEL).orEmpty()
        val timeText = intent.getStringExtra(AlarmConstants.EXTRA_ALARM_TIME).orEmpty().ifBlank { "07:30" }
        val rawDayBadge = intent.getStringExtra(AlarmConstants.EXTRA_DAY_BADGE).orEmpty()
        val snoozeMinutes = intent.getIntExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, 5)
        val isTest = intent.getBooleanExtra(AlarmConstants.EXTRA_IS_TEST, false)

        val prefsFlow = (applicationContext as HolidayAlarmApp).preferencesRepository.preferencesFlow

        setContent {
            val prefs by prefsFlow.collectAsStateWithLifecycle(initialValue = AppPreferences())
            val strings = StringPacks.forLanguage(prefs.appLanguage)
            val resolvedLabel = rawLabel.ifBlank { strings.appTitle }
            val resolvedBadge = if (rawDayBadge.isBlank()) {
                strings.ringingDefaultBadge
            } else {
                strings.localizeHolidayLabel(rawDayBadge, ChinaDayType.WORKDAY)
            }

            CompositionLocalProvider(LocalAppStrings provides strings) {
                HolidayAlarmTheme(
                    themeMode = ThemeMode.DARK,
                    themePalette = prefs.themePalette,
                    useDynamicColor = prefs.useDynamicColor
                ) {
                    RingingScreenContent(
                        timeText = timeText,
                        label = resolvedLabel,
                        dayBadge = resolvedBadge,
                        snoozeMinutes = snoozeMinutes,
                        isTest = isTest,
                        onSnooze = {
                            val svcIntent = Intent(this, AlarmRingingService::class.java).apply {
                                action = if (isTest) AlarmConstants.ACTION_DISMISS else AlarmConstants.ACTION_SNOOZE
                                putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
                                putExtra(AlarmConstants.EXTRA_ALARM_LABEL, resolvedLabel)
                                putExtra(AlarmConstants.EXTRA_SNOOZE_MINUTES, snoozeMinutes)
                            }
                            startService(svcIntent)
                            finish()
                        },
                        onDismiss = {
                            val svcIntent = Intent(this, AlarmRingingService::class.java).apply {
                                action = AlarmConstants.ACTION_DISMISS
                                putExtra(AlarmConstants.EXTRA_ALARM_ID, alarmId)
                            }
                            startService(svcIntent)
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun configureShowOverLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val km = getSystemService(KeyguardManager::class.java)
            km?.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}

@Composable
private fun RingingScreenContent(
    timeText: String,
    label: String,
    dayBadge: String,
    snoozeMinutes: Int,
    isTest: Boolean,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A193B),
                        Color(0xFF050A18),
                        Color(0xFF000000)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 32.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Pill (One UI 9 Now Bar Inspired)
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF1A2D5A).copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF7CB0FF),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (isTest) s.ringingTestStatus(dayBadge) else s.ringingTodayStatus(dayBadge),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFDCE8FF)
                    )
                }
            }

            // Center Clock & Pulse Halo
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF387AFF).copy(alpha = 0.35f),
                                        Color.Transparent
                                    )
                                ),
                                shape = CircleShape
                            )
                    )
                    Text(
                        text = timeText,
                        fontFamily = OutfitDisplayFamily,
                        fontSize = 84.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-2).sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFFDCE4F5)
                )
            }

            // Bottom Thumb-Zone Controls (One UI 9 Ergonomics)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FilledTonalButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .testTag("ringing_snooze_button"),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF1F2433),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = if (isTest) s.endTestPreview else s.snoozeAfterMinutes(snoozeMinutes),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .testTag("ringing_dismiss_button"),
                    shape = RoundedCornerShape(30.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF387AFF),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AlarmOff,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = s.dismissAlarm,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
        }
    }
}
