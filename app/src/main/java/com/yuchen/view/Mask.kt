package com.yuchen.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange

class Mask @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val arcRectF = RectF()
    private val paint = Paint()
    private var percentage = 0.0f
    private val xfermodeClear = PorterDuffXfermode(PorterDuff.Mode.CLEAR)

    fun setPercentage(percentage:Float){
        this.percentage = percentage
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        canvas?.apply {
            val backLayer = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

            paint.reset()
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#80000000")
            drawRoundRect(0f, 0f, width.toFloat(), height.toFloat(), 80f, 80f, paint)

            paint.reset()
            paint.style = Paint.Style.FILL
            paint.xfermode = xfermodeClear
            arcRectF.apply {
                left = 0f-width.toFloat()
                top = 0f-height.toFloat()
                right = width.toFloat()*2
                bottom = height.toFloat()*2
            }
            drawArc(arcRectF, 270f, 360f * percentage, true, paint)

            canvas.restoreToCount(backLayer)
        }
    }
}