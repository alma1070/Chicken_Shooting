package com.almaslowcore.chickenshooting

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF

class Opponent(var x: Float, var y: Float, var speed: Float, private val bitmap: Bitmap) {
    val width: Float = bitmap.width.toFloat()
    val height: Float = bitmap.height.toFloat()
    private val rect: RectF = RectF(x, y, x + width, y + height)

    fun update() {
        y += speed
        rect.set(x, y, x + width, y + height)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, null, rect, null)
    }

    fun isOffScreen(height: Int): Boolean {
        return y > height
    }

    fun getRect(): RectF {
        return rect
    }
}



