package com.privacydisplay

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class StripeView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
        isAntiAlias = false
    }
    private var stripePx = 4f

    fun configure(intensityPercent: Int, sizeDp: Int) {
        paint.alpha = (intensityPercent.coerceIn(0, 100) * 255) / 100
        stripePx = sizeDp * resources.displayMetrics.density
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        var y = 0f
        while (y < h) {
            canvas.drawRect(0f, y, w, y + stripePx, paint)
            y += stripePx * 2
        }
    }
}
