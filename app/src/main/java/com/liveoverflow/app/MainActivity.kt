package com.liveoverflow.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_start).setOnClickListener {
            if (Settings.canDrawOverlays(this)) {
                ContextCompat.startForegroundService(this, Intent(this, OverlayService::class.java))
                Toast.makeText(this, "🚀 小鲸鱼要出来啦", Toast.LENGTH_SHORT).show()
            } else {
                requestOverlayPermission()
            }
        }
    }

    private fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startActivity(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "✨ 权限已就绪，点开始吧", Toast.LENGTH_SHORT).show()
        }
    }
}
