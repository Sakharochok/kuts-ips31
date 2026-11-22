package com.kuts.battleship

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.kuts.battleship.ui.AppScreen
import com.kuts.battleship.ui.BattleshipTheme
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity created (restored=%s)", savedInstanceState != null)
        setContent {
            BattleshipTheme {
                AppScreen()
            }
        }
    }
}
