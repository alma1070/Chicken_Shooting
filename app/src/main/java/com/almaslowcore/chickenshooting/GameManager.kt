package com.almaslowcore.chickenshooting

import android.content.Context
import android.graphics.BitmapFactory

class GameManager(private val context: Context) {

    fun createOpponent(x: Float, y: Float, speed: Float): Opponent {
        // Load the custom image
        val customBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.token_red_emovebg)

        // Create an Opponent object with the loaded custom image
        return Opponent(x, y, speed, customBitmap)
    }
}
