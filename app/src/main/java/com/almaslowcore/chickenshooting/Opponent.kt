package com.almaslowcore.chickenshooting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

class Opponent(
    var x: Float,
    var y: Float,
    var speed: Float,
    private val bitmap: Bitmap,
    var maxHealth: Int = (1..3).random(),
    var currentHealth: Int = 0) {
    val width: Float = bitmap.width.toFloat()
    val height: Float = bitmap.height.toFloat()
    private val rect: RectF = RectF(x, y, x + width, y + height)
    private val healthBarPaint = Paint()
    init {
        currentHealth = maxHealth
    }
    fun update() {
        y += speed
        rect.set(x, y, x + width, y + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)

        val barWidth = width.toFloat()
        val barHeight = 10f
        val barTop = y - 20f // Position bar above the enemy

        healthBarPaint.color = Color.RED
        canvas.drawRect(x, barTop, x + barWidth, barTop + barHeight, healthBarPaint)

        val healthPercentage = currentHealth.toFloat() / maxHealth.toFloat()
        canvas.drawRect(x, barTop, x + (barWidth * healthPercentage), barTop + barHeight, healthBarPaint)
    }

    fun isOffScreen(height: Int): Boolean {
        return y > height
    }

    fun getRect(): RectF {
        return rect
    }
}



