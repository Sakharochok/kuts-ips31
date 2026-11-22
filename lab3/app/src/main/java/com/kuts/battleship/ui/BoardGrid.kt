package com.kuts.battleship.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuts.battleship.R
import com.kuts.battleship.game.CellState
import com.kuts.battleship.game.Coord

/** Позначення клітинки для повідомлень: літера стовпця з ресурсів поточної мови + номер рядка. */
@Composable
fun coordLabel(coord: Coord): String =
    "${stringResource(R.string.column_letters)[coord.col]}${coord.row + 1}"

/**
 * Поле 10x10 з підписами стовпців (літери) та рядків (цифри).
 * @param onCellClick null — поле лише для перегляду.
 */
@Composable
fun BoardGrid(
    cells: List<List<CellState>>,
    modifier: Modifier = Modifier,
    highlight: Coord? = null,
    onCellClick: ((Coord) -> Unit)? = null,
) {
    val letters = stringResource(R.string.column_letters)
    Column(modifier.widthIn(max = 360.dp).fillMaxWidth()) {
        Row {
            Label("")
            letters.take(cells.size).forEach { Label(it.toString(), Modifier.weight(1f)) }
        }
        cells.forEachIndexed { row, rowCells ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Label((row + 1).toString())
                rowCells.forEachIndexed { col, state ->
                    val coord = Coord(row, col)
                    Cell(
                        state = state,
                        highlighted = coord == highlight,
                        onClick = onCellClick?.let { click -> { click(coord) } },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun Label(text: String, modifier: Modifier = Modifier.width(LABEL_WIDTH)) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
    )
}

private val LABEL_WIDTH = 22.dp

@Composable
fun Cell(
    state: CellState,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val background = when (state) {
        CellState.SHIP -> BoardColors.ship
        CellState.HIT -> BoardColors.hit
        CellState.SUNK -> BoardColors.sunk
        CellState.EMPTY, CellState.MISS -> BoardColors.water
    }
    val symbol = when (state) {
        CellState.MISS -> "•"
        CellState.HIT, CellState.SUNK -> "✕"
        CellState.EMPTY, CellState.SHIP -> ""
    }
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(background)
            .border(
                width = if (highlighted) 2.dp else 0.5.dp,
                color = if (highlighted) BoardColors.lastShot else BoardColors.grid,
            )
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center,
    ) {
        if (symbol.isNotEmpty()) {
            Text(
                text = symbol,
                color = if (state == CellState.MISS) BoardColors.mark else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    }
}
