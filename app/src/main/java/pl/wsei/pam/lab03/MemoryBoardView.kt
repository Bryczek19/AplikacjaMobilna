package pl.wsei.pam.lab03

import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import pl.wsei.pam.lab01.R
import java.util.Stack

class MemoryBoardView(
    private val gridLayout: GridLayout,
    private val cols: Int,
    private val rows: Int
) {
    private val tiles: MutableMap<String, Tile> = mutableMapOf()

    private val icons: List<Int> = listOf(
        R.drawable.card_icon_1,
        R.drawable.card_icon_2,
        R.drawable.card_icon_3,
        R.drawable.card_icon_4,
        R.drawable.card_icon_5,
        R.drawable.card_icon_6,
        R.drawable.card_icon_7,
        R.drawable.card_icon_8,
        R.drawable.card_icon_9,
        R.drawable.card_icon_10,
        R.drawable.card_icon_11,
        R.drawable.card_icon_12,
        R.drawable.card_icon_13,
        R.drawable.card_icon_14,
        R.drawable.card_icon_15,
        R.drawable.card_icon_16,
        R.drawable.card_icon_17,
        R.drawable.card_icon_18
    )

    private val deckResource: Int = R.drawable.deck
    private var onGameChangeStateListener: (MemoryGameEvent) -> Unit = { _ -> }
    private val matchedPair: Stack<Tile> = Stack()
    private val logic: MemoryGameLogic = MemoryGameLogic(cols * rows / 2)

    private var isBoardLocked: Boolean = false

    init {
        gridLayout.columnCount = cols
        gridLayout.rowCount = rows
        gridLayout.setBackgroundColor(Color.parseColor("#F2F2F2"))

        val shuffledIcons: MutableList<Int> = mutableListOf<Int>().also {
            it.addAll(icons.subList(0, cols * rows / 2))
            it.addAll(icons.subList(0, cols * rows / 2))
            it.shuffle()
        }

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val btn = ImageButton(gridLayout.context).also {
                    it.tag = "${row}x${col}"

                    val layoutParams = GridLayout.LayoutParams().apply {
                        width = 0
                        height = 0
                        setMargins(dp(3), dp(3), dp(3), dp(3))
                        setGravity(Gravity.FILL)
                        columnSpec = GridLayout.spec(col, 1, 1f)
                        rowSpec = GridLayout.spec(row, 1, 1f)
                    }

                    it.layoutParams = layoutParams
                    it.setImageResource(deckResource)

                    styleButton(it)
                    gridLayout.addView(it)
                }

                val imageRes = shuffledIcons.removeAt(0)
                addTile(btn, imageRes)
            }
        }
    }

    private fun dp(value: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            value.toFloat(),
            gridLayout.context.resources.displayMetrics
        ).toInt()
    }

    private fun styleButton(button: ImageButton) {
        button.scaleType = ImageView.ScaleType.CENTER_INSIDE
        button.adjustViewBounds = true
        button.setPadding(dp(8), dp(8), dp(8), dp(8))
        button.setBackgroundResource(android.R.color.transparent)
        button.elevation = 0f
    }

    fun setBoardLocked(locked: Boolean) {
        isBoardLocked = locked
    }

    private fun onClickTile(v: View) {
        if (isBoardLocked) return

        val tile = tiles[v.tag] ?: return

        if (tile.revealed) return

        tile.revealed = true
        matchedPair.push(tile)

        val matchResult = logic.process {
            tile.tileResource
        }

        onGameChangeStateListener(MemoryGameEvent(matchedPair.toList(), matchResult))

        if (matchResult != GameStates.Matching) {
            matchedPair.clear()
        }
    }

    fun setOnGameChangeListener(listener: (event: MemoryGameEvent) -> Unit) {
        onGameChangeStateListener = listener
    }

    private fun addTile(button: ImageButton, resourceImage: Int) {
        button.setOnClickListener(::onClickTile)
        val tile = Tile(button, resourceImage, deckResource)
        tiles[button.tag.toString()] = tile
    }

    fun getState(): IntArray {
        val state = IntArray(cols * rows)
        var i = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val key = "${row}x${col}"
                val tile = tiles[key]
                state[i] = if (tile?.revealed == true) tile.tileResource else -1
                i++
            }
        }
        return state
    }

    fun setState(state: IntArray) {
        var i = 0
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val key = "${row}x${col}"
                val tile = tiles[key]
                if (tile != null) {
                    tile.revealed = state[i] != -1
                    if (tile.revealed) {
                        tile.removeOnClickListener()
                    }
                }
                i++
            }
        }
    }
}