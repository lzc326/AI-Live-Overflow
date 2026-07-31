package com.liveoverflow.app

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View
    private lateinit var whaleView: WhaleView
    private lateinit var bubbleView: View
    private lateinit var bubbleText: android.widget.TextView

    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    private val threshold = 12f

    private lateinit var supabase: SupabaseClient

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        supabase = SupabaseClient { text ->
            runOnUi {
                whaleView.react(text)
                showBubble(text)
            }
        }
        startForeground(1, buildNotification())
        setupOverlay()
        listenToBackend()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "overlay", "悬浮窗宠物", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "小鲸鱼在守护你"
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val pi = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, "overlay")
            .setContentTitle("🐋 小鲸鱼在这里")
            .setContentText("点我或把这个小可爱拖到别处")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    @SuppressLint("InflateParams", "ClickableViewAccessibility")
    private fun setupOverlay() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        overlayView = inflater.inflate(R.layout.overlay_view, null)
        whaleView = overlayView.findViewById(R.id.whale_view)
        bubbleView = overlayView.findViewById(R.id.bubble_container)
        bubbleText = overlayView.findViewById(R.id.bubble_text)

        val point = Point()
        windowManager.defaultDisplay.getSize(point)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            windowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = point.x / 2 - 80
            y = point.y - 400
        }

        overlayView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    showBubble("👋 干嘛呀宝贝")
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.rawX - initialTouchX
                    val dy = event.rawY - initialTouchY
                    if (!isDragging && (Math.abs(dx) > threshold || Math.abs(dy) > threshold)) {
                        isDragging = true
                    }
                    if (isDragging) {
                        params.x = initialX + dx.toInt()
                        params.y = initialY + dy.toInt()
                        windowManager.updateViewLayout(overlayView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        whaleView.bounce()
                        showBubble("🐋 蹭蹭你~")
                    } else {
                        hideBubble()
                    }
                    true
                }
                else -> false
            }
        }

        windowManager.addView(overlayView, params)
        whaleView.startIdleAnimation()
    }

    private fun windowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    private fun showBubble(text: String) {
        bubbleText.text = text
        bubbleView.visibility = View.VISIBLE
        bubbleView.postDelayed({ hideBubble() }, 2500)
    }

    private fun hideBubble() {
        bubbleView.visibility = View.GONE
    }

    private fun listenToBackend() {
        Thread {
            while (true) {
                try {
                    supabase.fetchLatestMessage()
                } catch (_: Exception) {}
                Thread.sleep(3000)
            }
        }.start()
    }

    private fun runOnUi(block: () -> Unit) {
        overlayView.post(block)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
        whaleView.stopAnimation()
    }
}
