package com.example.pulsedock

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val DarkBackground = Color(0xFF0D0F17)
val CardBackground = Color(0x991A1D2B)
val NeonPurple = Color(0xFF8A2BE2)
val NeonCyan = Color(0xFF00E5FF)
val GlowGreen = Color(0xFF00FF66)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkOverlayPermission()

        setContent {
            FuturisticDashboard(
                onStartServices = {
                    startService(Intent(this, EdgeLightingService::class.java))
                    startService(Intent(this, TaskbarOverlayService::class.java))
                }
            )
        }
    }

    private fun checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, 1234)
        }
    }
}

@Composable
fun FuturisticDashboard(onStartServices: () -> Unit) {
    var isServiceActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "PULSEDOCK",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Edge & Dual Launcher",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isServiceActive) "SERVICES ACTIVE" else "SERVICES IDLE",
                            fontWeight = FontWeight.Bold,
                            color = if (isServiceActive) GlowGreen else Color.Gray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Visualizer & Split Launcher",
                            color = Color.LightGray,
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = isServiceActive,
                        onCheckedChange = {
                            isServiceActive = it
                            if (it) onStartServices()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NeonPurple
                        )
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FeatureTile(title = "Muviz Edge Visualizer", subtitle = "Audio Dynamic Wave Engine", color = NeonPurple)
                FeatureTile(title = "Split Screen Dock", subtitle = "2-Tap Dual App Launcher", color = NeonCyan)
            }

            Text(
                text = "Pulsedock v1.0 | Built with GitHub Actions",
                fontSize = 10.sp,
                color = Color.DarkGray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun GlassCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardBackground)
            .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.Transparent)), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        content()
    }
}

@Composable
fun FeatureTile(title: String, subtitle: String, color: Color) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}
