package com.example.pulsedock

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView

class TaskbarOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var arrowTriggerView: View
    private lateinit var mainPanelContainer: View
    private var selectedAppPair = mutableListOf<String>()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        setupArrowTrigger()
        setupMainPanel()
    }

    private fun setupArrowTrigger() {
        arrowTriggerView = FrameLayout(this).apply {
            setBackgroundColor(0xFF8A2BE2.toInt())
        }

        val params = WindowManager.LayoutParams(
            100, 160,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
        }

        arrowTriggerView.setOnClickListener {
            toggleMainPanelVisibility()
        }

        windowManager.addView(arrowTriggerView, params)
    }

    private fun setupMainPanel() {
        val layoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mainPanelContainer = FrameLayout(this)

        val containerCard = CardView(this).apply {
            radius = 30f
            setCardBackgroundColor(0xEF131521.toInt())
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val title = TextView(this).apply {
            text = "SELECT 2 APPS FOR SPLIT SCREEN"
            setTextColor(0xFF00E5FF.toInt())
            textSize = 12f
            setTypeface(null, android.graphics.Typeface.BOLD)
        }

        val appListView = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA).take(6)

        for (app in packages) {
            val iconView = ImageView(this).apply {
                setImageDrawable(pm.getApplicationIcon(app))
                layoutParams = LinearLayout.LayoutParams(110, 110).apply {
                    setMargins(8, 0, 8, 0)
                }
                setOnClickListener {
                    handleAppSelection(app.packageName)
                }
            }
            appListView.addView(iconView)
        }

        layout.addView(title)
        layout.addView(appListView)
        containerCard.addView(layout)
        (mainPanelContainer as FrameLayout).addView(containerCard)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            500,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
        }

        mainPanelContainer.visibility = View.GONE
        windowManager.addView(mainPanelContainer, params)
    }

    private fun handleAppSelection(packageName: String) {
        selectedAppPair.add(packageName)

        if (selectedAppPair.size == 1) {
            Toast.makeText(this, "Select 2nd App for Split Screen", Toast.LENGTH_SHORT).show()
        } else if (selectedAppPair.size == 2) {
            launchSplitPair(selectedAppPair[0], selectedAppPair[1])
            selectedAppPair.clear()
            mainPanelContainer.visibility = View.GONE
        }
    }

    private fun launchSplitPair(app1: String, app2: String) {
        val intent1 = packageManager.getLaunchIntentForPackage(app1)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent1)

        val intent2 = packageManager.getLaunchIntentForPackage(app2)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT)
        }
        startActivity(intent2)
    }

    private fun toggleMainPanelVisibility() {
        if (mainPanelContainer.visibility == View.VISIBLE) {
            mainPanelContainer.visibility = View.GONE
        } else {
            mainPanelContainer.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::arrowTriggerView.isInitialized) windowManager.removeView(arrowTriggerView)
        if (::mainPanelContainer.isInitialized) windowManager.removeView(mainPanelContainer)
    }
}
