package pl.wsei.pam.lab03

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R

class Lab03Activity : AppCompatActivity() {

    private lateinit var mBoard: GridLayout
    private lateinit var mBoardModel: MemoryBoardView

    private var cols: Int = 3
    private var rows: Int = 3

    private var isSound = true

    private lateinit var completionPlayer: MediaPlayer
    private lateinit var negativePlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab03)

        mBoard = findViewById(R.id.boardGrid)

        if (savedInstanceState != null) {
            cols = savedInstanceState.getInt("columns", 3)
            rows = savedInstanceState.getInt("rows", 3)
        } else {
            cols = intent.getIntExtra("columns", 3)
            rows = intent.getIntExtra("rows", 3)
        }

        mBoardModel = MemoryBoardView(mBoard, cols, rows)

        mBoardModel.setOnGameChangeListener { e ->
            when (e.state) {
                GameStates.Matching -> {
                    e.tiles.forEach { it.revealed = true }
                }

                GameStates.Match -> {
                    e.tiles.forEach { it.revealed = true }
                    mBoardModel.setBoardLocked(true)

                    if (isSound) {
                        playCompletionSound()
                    }

                    if (e.tiles.size >= 2) {
                        animatePairedTile(e.tiles[0]) {
                            animatePairedTile(e.tiles[1]) {
                                e.tiles.forEach { it.removeOnClickListener() }
                                mBoardModel.setBoardLocked(false)
                            }
                        }
                    } else {
                        e.tiles.forEach { it.removeOnClickListener() }
                        mBoardModel.setBoardLocked(false)
                    }
                }

                GameStates.NoMatch -> {
                    e.tiles.forEach { it.revealed = true }
                    mBoardModel.setBoardLocked(true)

                    if (isSound) {
                        playNegativeSound()
                    }

                    if (e.tiles.size >= 2) {
                        animateWrongPair(e.tiles[0], e.tiles[1]) {
                            e.tiles.forEach { it.revealed = false }
                            mBoardModel.setBoardLocked(false)
                        }
                    } else {
                        e.tiles.forEach { it.revealed = false }
                        mBoardModel.setBoardLocked(false)
                    }
                }

                GameStates.Finished -> {
                    e.tiles.forEach { it.revealed = true }
                    mBoardModel.setBoardLocked(true)

                    if (isSound) {
                        playCompletionSound()
                    }

                    if (e.tiles.size >= 2) {
                        animatePairedTile(e.tiles[0]) {
                            animatePairedTile(e.tiles[1]) {
                                e.tiles.forEach { it.removeOnClickListener() }
                                Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        e.tiles.forEach { it.removeOnClickListener() }
                        Toast.makeText(this, "Game finished", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        if (savedInstanceState != null) {
            val state = savedInstanceState.getIntArray("board_state")
            if (state != null) {
                mBoardModel.setState(state)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        completionPlayer = MediaPlayer.create(applicationContext, R.raw.completion)
        negativePlayer = MediaPlayer.create(applicationContext, R.raw.negative_guitar)

        completionPlayer.setVolume(1.0f, 1.0f)
        negativePlayer.setVolume(1.0f, 1.0f)
    }

    override fun onPause() {
        super.onPause()

        if (::completionPlayer.isInitialized) {
            completionPlayer.release()
        }

        if (::negativePlayer.isInitialized) {
            negativePlayer.release()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.board_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.board_activity_sound -> {
                isSound = !isSound

                if (isSound) {
                    item.setIcon(R.drawable.baseline_volume_up_24)
                    Toast.makeText(this, "Sound turn on", Toast.LENGTH_SHORT).show()
                } else {
                    item.setIcon(R.drawable.baseline_volume_off_24)
                    Toast.makeText(this, "Sound turn off", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putIntArray("board_state", mBoardModel.getState())
        outState.putInt("columns", cols)
        outState.putInt("rows", rows)
    }

    private fun playCompletionSound() {
        if (::completionPlayer.isInitialized) {
            if (completionPlayer.isPlaying) {
                completionPlayer.seekTo(0)
            }
            completionPlayer.start()
        }
    }

    private fun playNegativeSound() {
        if (::negativePlayer.isInitialized) {
            if (negativePlayer.isPlaying) {
                negativePlayer.seekTo(0)
            }
            negativePlayer.start()
        }
    }

    private fun animatePairedTile(tile: Tile, action: () -> Unit) {
        val button = tile.button

        val rotation = ObjectAnimator.ofFloat(button, "rotation", 0f, 25f, -25f, 0f)
        val scaleX = ObjectAnimator.ofFloat(button, "scaleX", 1f, 3.5f)
        val scaleY = ObjectAnimator.ofFloat(button, "scaleY", 1f, 3.5f)
        val fade = ObjectAnimator.ofFloat(button, "alpha", 1f, 0f)

        val set = AnimatorSet()
        set.duration = 650
        set.interpolator = DecelerateInterpolator()
        set.playTogether(rotation, scaleX, scaleY, fade)

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                button.rotation = 0f
                button.scaleX = 1f
                button.scaleY = 1f
                button.alpha = 0f
                action()
            }
        })

        set.start()
    }

    private fun animateWrongPair(firstTile: Tile, secondTile: Tile, action: () -> Unit) {
        val firstButton = firstTile.button
        val secondButton = secondTile.button

        val firstRotation = ObjectAnimator.ofFloat(
            firstButton,
            "rotation",
            0f, -8f, 8f, -8f, 8f, 0f
        )

        val secondRotation = ObjectAnimator.ofFloat(
            secondButton,
            "rotation",
            0f, 8f, -8f, 8f, -8f, 0f
        )

        val set = AnimatorSet()
        set.duration = 450
        set.interpolator = DecelerateInterpolator()
        set.playTogether(firstRotation, secondRotation)

        set.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                firstButton.rotation = 0f
                secondButton.rotation = 0f
                action()
            }
        })

        set.start()
    }
}