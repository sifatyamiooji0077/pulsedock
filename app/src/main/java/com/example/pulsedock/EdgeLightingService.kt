package com.example.pulsedock

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.media.audiofx.Visualizer
import android.os.Build
import android.os.IBinder
import android.view.View
import android.view.WindowManager

class EdgeLightingService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var edgeCanvasView: EdgeCanvasView
    private var visualizer: Visualizer? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        edgeCanvasView = EdgeCanvasView(this)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            PixelFormat.TRANSLUCENT
        )

        windowManager.addView(edgeCanvasView, params)
        initAudioVisualizer()
    }

    private fun initAudioVisualizer() {
        try {
            visualizer = Visualizer(0).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {
                        waveform?.let { edgeCanvasView.updateWaveform(it) }
                    }

                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {}
                }, Visualizer.getMaxCaptureRate() / 2, true, false)
                enabled = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        visualizer?.release()
        if (::edgeCanvasView.isInitialized) windowManager.removeView(edgeCanvasView)
    }

    private class EdgeCanvasView(context: Context) : View(context) {
        private val paint = Paint().apply {
            strokeWidth = 20f
            style = Paint.Style.STROKE
            isAntiAlias = true
            pathEffect = CornerPathEffect(40f)
        }

        private var magnitude = 0f

        fun updateWaveform(waveform: ByteArray) {
            val raw = waveform[0].toInt()
            magnitude = Math.abs(raw).toFloat()
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)

            val shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF8A2BE2.toInt(), 0xFF00E5FF.toInt(), 0xFF00FF66.toInt()),
                null, Shader.TileMode.MIRROR
            )
            paint.shader = shader
            paint.strokeWidth = 10f + (magnitude % 15f)

            val rect = RectF(10f, 10f, width - 10f, height - 10f)
            canvas.drawRoundRect(rect, 50f, 50f, paint)
        }
    }
}
