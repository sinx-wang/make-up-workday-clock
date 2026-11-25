package com.example.ui.i18n

import java.util.Locale

/**
 * Supported in-app display languages.
 * To add a new language in the future:
 * 1. Add an enum entry here with its BCP-47 tag and native display name.
 * 2. Provide an [AppStrings] implementation and map it in [StringPacks.forLanguage].
 */
enum class AppLanguage(
    val languageTag: String,
    val nativeName: String
) {
    SYSTEM("system", "System"),
    ZH_CN("zh-CN", "简体中文"),
    EN("en", "English");

    companion object {
        fun fromName(name: String?): AppLanguage =
            entries.firstOrNull { it.name == name } ?: ZH_CN

        fun resolveEffective(language: AppLanguage, systemLocale: Locale = Locale.getDefault()): AppLanguage {
            return if (language == SYSTEM) {
                when (systemLocale.language.lowercase(Locale.ROOT)) {
                    "zh" -> ZH_CN
                    else -> EN
                }
            } else {
                language
            }
        }
    }
}
