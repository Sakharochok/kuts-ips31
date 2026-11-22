package com.kuts.battleship.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Navy = Color(0xFF1565C0)
private val Sky = Color(0xFF90CAF9)

@Composable
fun BattleshipTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) {
        darkColorScheme(primary = Sky, secondary = Sky)
    } else {
        lightColorScheme(primary = Navy, secondary = Navy)
    }
    MaterialTheme(colorScheme = colors, content = content)
}

/** Кольори клітинок поля — однакові у світлій і темній темі, щоб поле читалося як "море". */
object BoardColors {
    val water = Color(0xFFE3F2FD)
    val grid = Color(0xFF64B5F6)
    val ship = Color(0xFF546E7A)
    val hit = Color(0xFFFF7043)
    val sunk = Color(0xFFB71C1C)
    val mark = Color(0xFF1E3A5F)
    val lastShot = Color(0xFFFFC107)
}
