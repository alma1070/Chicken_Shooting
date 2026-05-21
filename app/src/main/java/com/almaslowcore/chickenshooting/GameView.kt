package com.almaslowcore.chickenshooting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.random.Random

class GameView(context: Context, attrs: AttributeSet? = null) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.galaxy_background)
    private var backgroundScaledBitmap: Bitmap? = null
    private val thread: GameThread
    private val firingObjects = mutableListOf<FiringObject>()
    private val opponents = mutableListOf<Opponent>()
    private var score: Int = 0
    private var gameOver: Boolean = false

    // Reduce the initial speed of opponents
    private var opponentBaseSpeed = 5f
    private var firingObjectBaseSpeed = 20f

    // >>>ex1>>>
    private var highScore: Int = 0
    private val sharedPreferences = context.getSharedPreferences("ChickenShootingPrefs", Context.MODE_PRIVATE)
    // <<<ex1<<<

    // >>>ex2>>>
    private var lives = 3
    // <<<ex2<<<

    // >>>ex1>>>
    private val highScorePaint: Paint = Paint().apply {
        color = Color.YELLOW
        textSize = 50f
        textAlign = Paint.Align.RIGHT
    }
    // <<<ex1<<<
    private val scorePaint: Paint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
    }

    private val gameOverPaint: Paint = Paint().apply {
        color = Color.RED
        textSize = 100f
    }

    // >>>ex2>>>
    private val livesPaint = Paint().apply {
        color = Color.RED
        textSize = 50f
        isFakeBoldText = true
        textAlign = Paint.Align.RIGHT
    }
    // <<<ex2<<<

    // List of opponent bitmaps
    private val opponentBitmaps: List<Bitmap> = listOf(
        BitmapFactory.decodeResource(resources, R.drawable.rocket),
        BitmapFactory.decodeResource(resources, R.drawable.rocket_2),
        BitmapFactory.decodeResource(resources, R.drawable.alian)
        // Add more images as needed
    ).filterNotNull() // Ensure no null bitmaps are added

    init {
        holder.addCallback(this)
        thread = GameThread(holder, this)
        // >>>ex1>>>
        highScore = sharedPreferences.getInt("highScore", 0)
        // <<<ex1<<<
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        backgroundScaledBitmap = Bitmap.createScaledBitmap(backgroundBitmap, width, height, true)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        thread.running = false
        while (retry) {
            try {
                thread.join()
                retry = false
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        thread.running = true
        thread.start()
    }

    @Synchronized
    fun update() {
        if (gameOver) return

        // Further reduce the rate of speed increase
        opponentBaseSpeed += score * 0.0001f
        firingObjectBaseSpeed += score * 0.0005f

        // Cap the maximum speed (optional)
        val maxSpeed = 15f
        if (opponentBaseSpeed > maxSpeed) {
            opponentBaseSpeed = maxSpeed
        }
        if (firingObjectBaseSpeed > maxSpeed) {
            firingObjectBaseSpeed = maxSpeed
        }

        firingObjects.forEach { it.update() }
        firingObjects.removeAll { it.isOffScreen(height) }

        opponents.forEach { it.update() }
        opponents.removeAll { it.isOffScreen(height) }

        val firingObjectsCopy = ArrayList(firingObjects)
        val opponentsCopy = ArrayList(opponents)

        for (firingObject in firingObjectsCopy) {
            for (opponent in opponentsCopy) {
                if (firingObject.rect.intersect(opponent.getRect())) {
                    firingObjects.remove(firingObject)
                    opponents.remove(opponent)
                    score += 10
                    break
                }
            }
        }

        // >>>ex2>>>
        val iterator = opponents.iterator()
        while (iterator.hasNext()) {
            val opponent = iterator.next()
            opponent.update()

            if (opponent.y + opponent.height >= height - 100) {
                lives--

                if (lives <= 0) {
                    gameOver = true
                    // >>>ex1>>>
                    if (score > highScore) {
                        highScore = score
                        // save to permanent storage
                        sharedPreferences.edit().putInt("highScore", highScore).apply()
                    }
                    // <<<ex1<<<
                } else {
                    iterator.remove()
                }
                break
            }
        }
        for (opponent in opponents) {

        }

        if (Random.nextFloat() < 0.02) {
            val randomBitmap = opponentBitmaps.randomOrNull()
            if (randomBitmap != null) {
                // Resize the bitmap if needed
                val resizedBitmap = Bitmap.createScaledBitmap(randomBitmap, 100, 100, false)
                val opponent = Opponent(Random.nextFloat() * (width - 80), -80f, opponentBaseSpeed, resizedBitmap)
                opponents.add(opponent)
            }
        }
    }

    @Synchronized
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawBackground(canvas)

        // Draw custom images for opponents
        opponents.forEach { it.draw(canvas) }

        // Draw other elements like firing objects and score
        firingObjects.forEach { it.draw(canvas) }
        canvas.drawText("Score: $score", 50f, 100f, scorePaint)

        // >>>ex1>>> Draw high score
        canvas.drawText("High Score: $highScore", width - 50f, 100f, highScorePaint)
        // <<<ex1<<<

        // >>>ex2>>>
        val livesText = "Lives: $lives"
        canvas.drawText(livesText, width - 50f, 150f, livesPaint)
        // <<<ex2<<<

        if (gameOver) {
            canvas.drawText("Game Over", width / 2f - 200f, height / 2f, gameOverPaint)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        backgroundScaledBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, null)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (gameOver) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                resetGame()
                return true
            }
            return super.onTouchEvent(event)
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            val firingObject = FiringObject(event.x, height - 100f, firingObjectBaseSpeed)
            synchronized(this) {
                firingObjects.add(firingObject)
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun resetGame() {
        score = 0
        // >>>ex2>>>
        lives = 3
        // <<<ex2<<<
        // Reset the speed of opponents and firing objects
        opponentBaseSpeed = 5f
        firingObjectBaseSpeed = 20f
        synchronized(this) {
            firingObjects.clear()
            opponents.clear()
        }
        gameOver = false
    }
}
