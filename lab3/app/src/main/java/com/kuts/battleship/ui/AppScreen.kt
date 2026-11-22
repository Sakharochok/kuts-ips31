package com.kuts.battleship.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuts.battleship.R

/** Кореневий екран: спільна верхня панель і перемикання між екранами застосунку. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen(viewModel: GameViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.screen != Screen.MENU) { viewModel.onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // У меню назва гри вже є під логотипом, тому панель лишається без заголовка.
                    val title = when (state.screen) {
                        Screen.MENU -> null
                        Screen.RULES -> R.string.rules_title
                        Screen.PLACEMENT -> R.string.placement_title
                        Screen.BATTLE -> R.string.battle_title
                    }
                    if (title != null) Text(stringResource(title))
                },
                navigationIcon = {
                    if (state.screen != Screen.MENU) {
                        IconButton(onClick = { viewModel.onBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.cd_back))
                        }
                    }
                },
                actions = {
                    if (state.screen != Screen.RULES) {
                        IconButton(onClick = viewModel::onShowRules) {
                            Icon(Icons.Outlined.Info, stringResource(R.string.cd_rules))
                        }
                    }
                    LanguageMenu()
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state.screen) {
            Screen.MENU -> MenuScreen(
                canContinue = state.canContinue,
                onNewGame = viewModel::onNewGame,
                onContinue = viewModel::onContinue,
                onRules = viewModel::onShowRules,
                modifier = modifier,
            )
            Screen.RULES -> RulesScreen(onDone = { viewModel.onBack() }, modifier = modifier)
            Screen.PLACEMENT -> PlacementScreen(state.placement, viewModel, modifier)
            Screen.BATTLE -> state.battle?.let { BattleScreen(it, viewModel, modifier) }
        }
    }
}
