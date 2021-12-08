package com.yuchen.view

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.util.AttributeSet
import android.view.View
import com.yuchen.bindingclick.R


class SwitchButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val textPaint = Paint().apply {
        textSize = 100f
        color = Color.GREEN
        textAlign = Paint.Align.CENTER
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    }

    private val paint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val iconPaint = Paint()

    private val drawable = resources.getDrawable(R.drawable.ic_ig_countdown_3, null)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        textPaint.color = Color.BLUE
//        canvas?.drawText("text", (left.toFloat()+right.toFloat())/2f, (top.toFloat()+bottom.toFloat())/2f, textPaint)

//        drawable.setColorFilter(Color.BLACK,PorterDuff.Mode.SRC)
        val bitmap = BitmapFactory.decodeResource(resources,R.drawable.ic_ig_countdown_3)
        bitmap?.let {
            canvas?.drawBitmap(
                it,
                left.toFloat(),
                top.toFloat(),
                null
            )
        }

        val backLayer = canvas?.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)
        paint.isAntiAlias = true
        canvas?.drawOval(
            (left.toFloat() + right.toFloat()) / 2f,
            top.toFloat(),
            right.toFloat(),
            (bottom.toFloat() + top.toFloat()) * 5f / 8f,
            paint
        )
//        textPaint.color = Color.GREEN
//        canvas?.drawText("text", (left.toFloat()+right.toFloat())/2f, (top.toFloat()+bottom.toFloat())/2f, textPaint)
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}