package com.lleyton.flightdeck.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lleyton.flightdeck.data.FlightMode
import com.lleyton.flightdeck.nativebridge.PathMath

@Composable
fun FlightScreen(viewModel: FlightViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val t = state.telemetry

    Column(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0E1116)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Heads-up display: the numbers a pilot glances at.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            HudValue("ALT", "%.0f m".format(t.altitudeM))
            HudValue("SPD", "%.1f m/s".format(t.speedMps))
            HudValue("BAT", "%.0f%%".format(t.batteryPct))
            HudValue("MODE", t.mode.name)
        }

        // Top-down map of the flight path (home point = center).
        Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
            val center = Offset(size.width / 2, size.height / 2)
            val metersToPx = size.minDimension / 140f

            fun toScreen(x: Float, y: Float) = Offset(center.x + x * metersToPx, center.y - y * metersToPx)

            drawCircle(Color(0xFF2E7D32), radius = 10f, center = center) // home

            val xs = state.pathXs.toFloatArray()
            val ys = state.pathYs.toFloatArray()
            if (xs.size > 1) {
                val smooth = PathMath.smoothPath(xs, ys, 5) // C++ via NDK
                val path = Path().apply {
                    val first = toScreen(smooth[0], smooth[1])
                    moveTo(first.x, first.y)
                    for (i in 1 until xs.size) {
                        val p = toScreen(smooth[2 * i], smooth[2 * i + 1])
                        lineTo(p.x, p.y)
                    }
                }
                drawPath(path, Color(0xFF64B5F6), style = Stroke(width = 4f))
            }
            drawCircle(Color.White, radius = 14f, center = toScreen(t.x, t.y)) // drone
        }

        // One-tap commands instead of joysticks.
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::takeOff, enabled = t.mode == FlightMode.LANDED) { Text("Take off") }
            Button(onClick = viewModel::orbit, enabled = t.mode != FlightMode.LANDED) { Text("Orbit") }
            Button(onClick = viewModel::returnHome, enabled = t.mode != FlightMode.LANDED) { Text("Return home") }
        }
    }
}

@Composable
private fun HudValue(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF90A4AE), style = MaterialTheme.typography.labelSmall)
        Text(value, color = Color.White, style = MaterialTheme.typography.titleMedium)
    }
}
