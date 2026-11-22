package com.kuts.battleship.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.kuts.battleship.R
import timber.log.Timber

/** Мова застосунку; порожній тег — мова системи. */
enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    ENGLISH("en"),
    UKRAINIAN("uk");

    companion object {
        fun current(): AppLanguage {
            val tag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return entries.firstOrNull { it != SYSTEM && tag.startsWith(it.tag) } ?: SYSTEM
        }
    }
}

/**
 * Перемикач мови в панелі застосунку. Використовує per-app language API з AppCompat:
 * вибір зберігається (див. AppLocalesMetadataHolderService у маніфесті),
 * а Activity перестворюється з новими ресурсами; стан гри тримає ViewModel.
 */
@Composable
fun LanguageMenu() {
    var expanded by remember { mutableStateOf(false) }
    val current = AppLanguage.current()

    Box {
        TextButton(onClick = { expanded = true }) {
            Text(
                when (current) {
                    AppLanguage.SYSTEM -> stringResource(R.string.language_menu)
                    else -> current.tag.uppercase()
                }
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Text(
                            when (language) {
                                AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                                AppLanguage.ENGLISH -> stringResource(R.string.language_en)
                                AppLanguage.UKRAINIAN -> stringResource(R.string.language_uk)
                            }
                        )
                    },
                    onClick = {
                        expanded = false
                        if (language != current) {
                            Timber.i("Language changed: %s -> %s", current, language)
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
                        }
                    },
                )
            }
        }
    }
}
