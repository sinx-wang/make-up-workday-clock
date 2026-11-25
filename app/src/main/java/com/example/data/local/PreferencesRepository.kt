package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.i18n.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "holiday_alarm_prefs")

enum class ThemeMode(val label: String) {
    SYSTEM("跟随系统"),
    DARK("深色"),
    LIGHT("浅色");

    companion object {
        fun fromName(name: String?): ThemeMode =
            entries.firstOrNull { it.name == name } ?: SYSTEM
    }
}

enum class ThemePalette(
    val displayName: String,
    val seedColorHex: Long
) {
    COBALT("钴蓝", 0xFF387AFF),
    EMERALD("翡翠", 0xFF10B981),
    VIOLET("星紫", 0xFF8B6CFF),
    CORAL("珊瑚", 0xFFFF5E62),
    AMBER("琥珀", 0xFFF59E0B),
    CYAN("冰青", 0xFF06B6D4),
    ROSE("樱粉", 0xFFEC4899);

    companion object {
        fun fromName(name: String?): ThemePalette =
            entries.firstOrNull { it.name == name } ?: COBALT
    }
}

enum class AutoSyncInterval(val label: String, val intervalHours: Long) {
    DAILY("每天", 24L),
    WEEKLY("每周", 24L * 7L),
    MANUAL("仅手动", Long.MAX_VALUE);

    companion object {
        fun fromName(name: String?): AutoSyncInterval =
            entries.firstOrNull { it.name == name } ?: DAILY
    }
}

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val themePalette: ThemePalette = ThemePalette.COBALT,
    val useDynamicColor: Boolean = false,
    val appLanguage: AppLanguage = AppLanguage.ZH_CN,
    val autoSyncInterval: AutoSyncInterval = AutoSyncInterval.DAILY,
    val lastSyncMillis: Long = 0L,
    val lastSyncSource: String = "内置基准",
    val lastSyncYears: String = "2025, 2026, 2027",
    val showTomorrowStatusBanner: Boolean = true,
    val oneUiExpandedHeader: Boolean = true,
    val defaultSnoozeMinutes: Int = 5
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val THEME_PALETTE = stringPreferencesKey("theme_palette")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val AUTO_SYNC_INTERVAL = stringPreferencesKey("auto_sync_interval")
        val LAST_SYNC_MILLIS = longPreferencesKey("last_sync_millis")
        val LAST_SYNC_SOURCE = stringPreferencesKey("last_sync_source")
        val LAST_SYNC_YEARS = stringPreferencesKey("last_sync_years")
        val SHOW_TOMORROW_BANNER = booleanPreferencesKey("show_tomorrow_banner")
        val ONE_UI_EXPANDED_HEADER = booleanPreferencesKey("one_ui_expanded_header")
        val DEFAULT_SNOOZE_MINUTES = intPreferencesKey("default_snooze_minutes")
    }

    val preferencesFlow: Flow<AppPreferences> = context.dataStore.data.map { prefs ->
        AppPreferences(
            themeMode = ThemeMode.fromName(prefs[Keys.THEME_MODE]),
            themePalette = ThemePalette.fromName(prefs[Keys.THEME_PALETTE]),
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: false,
            appLanguage = AppLanguage.fromName(prefs[Keys.APP_LANGUAGE]),
            autoSyncInterval = AutoSyncInterval.fromName(prefs[Keys.AUTO_SYNC_INTERVAL]),
            lastSyncMillis = prefs[Keys.LAST_SYNC_MILLIS] ?: 0L,
            lastSyncSource = prefs[Keys.LAST_SYNC_SOURCE] ?: "内置基准",
            lastSyncYears = prefs[Keys.LAST_SYNC_YEARS] ?: "2025, 2026, 2027",
            showTomorrowStatusBanner = prefs[Keys.SHOW_TOMORROW_BANNER] ?: true,
            oneUiExpandedHeader = prefs[Keys.ONE_UI_EXPANDED_HEADER] ?: true,
            defaultSnoozeMinutes = prefs[Keys.DEFAULT_SNOOZE_MINUTES] ?: 5
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setThemePalette(palette: ThemePalette) {
        context.dataStore.edit {
            it[Keys.THEME_PALETTE] = palette.name
            it[Keys.USE_DYNAMIC_COLOR] = false
        }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.APP_LANGUAGE] = language.name }
    }

    suspend fun setAutoSyncInterval(interval: AutoSyncInterval) {
        context.dataStore.edit { it[Keys.AUTO_SYNC_INTERVAL] = interval.name }
    }

    suspend fun setOneUiExpandedHeader(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ONE_UI_EXPANDED_HEADER] = enabled }
    }

    suspend fun setDefaultSnoozeMinutes(minutes: Int) {
        context.dataStore.edit { it[Keys.DEFAULT_SNOOZE_MINUTES] = minutes }
    }

    suspend fun recordSyncSuccess(source: String, yearsLabel: String, timestampMillis: Long = System.currentTimeMillis()) {
        context.dataStore.edit {
            it[Keys.LAST_SYNC_MILLIS] = timestampMillis
            it[Keys.LAST_SYNC_SOURCE] = source
            it[Keys.LAST_SYNC_YEARS] = yearsLabel
        }
    }
}
