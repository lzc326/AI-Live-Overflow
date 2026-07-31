package com.liveoverflow.app

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class WhaleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(77, 107, 254)
        style = Paint.Style.FILL
    }
    private val lightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(110, 134, 255)
        style = Paint.Style.FILL
    }
    private val eyePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }
    private val toothPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }

    private var floatOffset = 0f
    private var animator: ValueAnimator? = null

    init {
        startIdleAnimation()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h / 2f + floatOffset * 6f

        // 尾鳍
        val tail = Path().apply {
            moveTo(cx + w * 0.30f, cy + h * 0.05f)
            lineTo(cx + w * 0.42f, cy - h * 0.22f)
            lineTo(cx + w * 0.44f, cy + h * 0.12f)
            lineTo(cx - w * 0.42f, cy - h * 0.22f)
            close()
        }
        canvas.drawPath(tail, bodyPaint)

        // 身体
        val body = Path().apply {
            moveTo(cx - w * 0.38f, cy - h * 0.15f)
            quadTo(cx - w * 0.42f, cy, cx - w * 0.15f, cy + h * 0.18f)
            quadTo(cx, cy + h * 0.30f, cx + w * 0.25f, cy + h * 0.12f)
            quadTo(cx + w * 0.40f, cy, cx + w * 0.30f, cy - h * 0.12f)
            quadTo(cx + w * 0.10f, cy - h * 0.30f, cx - w * 0.30f, cy - h * 0.22f)
            close()
        }
        canvas.drawPath(body, bodyPaint)

        // 头部（偏大圆润）
        val head = Path().apply {
            moveTo(cx - w * 0.05f, cy - h * 0.30f)
            quadTo(cx + w * 0.18f, cy - h * 0.36f, cx + w * 0.30f, cy - h * 0.10f)
            quadTo(cx + w * 0.34f, cy + h * 0.10f, cx + w * 0.15f, cy + h * 0.16f)
            quadTo(cx, cy + h * 0.22f, cx - w * 0.10f, cy + h * 0.14f)
            quadTo(cx - w * 0.30f, cy + h * 0.02f, cx - w * 0.28f, cy - h * 0.12f)
            quadTo(cx - w * 0.22f, cy - h * 0.30f, cx - w * 0.05f, cy - h * 0.30f)
            close()
        }
        canvas.drawPath(head, lightPaint)

        // 眼睛
        canvas.drawCircle(cx + w * 0.14f, cy - h * 0.12f, w * 0.035f, eyePaint)

        // 嘴微张 + 白牙
        val mouth = Path().apply {
            moveTo(cx + w * 0.02f, cy + h * 0.04f)
            quadTo(cx + w * 0.14f, cy + h * 0.10f, cx + w * 0.22f, cy + h * 0.04f)
            lineTo(cx + w * 0.22f, cy + h * 0.10f)
            quadTo(cx + w * 0.14f, cy + h * 0.16f, cx + w * 0.02f, cy + h * 0.10f)
            close()
        }
        canvas.drawPath(mouth, bodyPaint)
        canvas.drawRect(
            cx + w * 0.06f, cy + h * 0.06f,
            cx + w * 0.20f, cy + h * 0.09f,
            toothPaint
        )
    }

    fun startIdleAnimation() {
        if (animator?.isRunning == true) return
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 2600
            repeatCount = ValueAnimator.INFINITE
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                floatOffset = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    fun stopAnimation() {
        animator?.cancel()
        animator = null
    }

    fun bounce() {
        stopAnimation()
        ValueAnimator.ofFloat(0f, 1f, 0f).apply {
            duration = 320
            addUpdateListener {
                floatOffset = (it.animatedValue as Float)
                invalidate()
            }
            start()
        }
        startIdleAnimation()
    }

    fun react(text: String) {
        bounce()
    }
}
