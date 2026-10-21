package com.lleyton.flightdeck.data

import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * Pure-Kotlin flight simulator. No Android dependencies, so it is easy to unit test.
 * step() takes the previous state and returns the next one after dtSec seconds.
 */
class TelemetrySimulator(
    private val cruiseAltitudeM: Float = 30f,
    private val climbRateMps: Float = 2f,
    private val orbitRadiusM: Float = 50f,
    private val orbitRateRadPerSec: Float = 0.2f,
    private val returnSpeedMps: Float = 8f,
    private val batteryDrainPctPerSec: Float = 0.05f,
    private val lowBatteryReturnPct: Float = 20f,
) {
    fun step(prev: Telemetry, dtSec: Float): Telemetry {
        val drained = if (prev.mode == FlightMode.LANDED) prev.batteryPct
                      else max(0f, prev.batteryPct - batteryDrainPctPerSec * dtSec)

        val next = when (prev.mode) {
            FlightMode.LANDED -> prev.copy(speedMps = 0f)

            FlightMode.TAKEOFF -> {
                val alt = min(cruiseAltitudeM, prev.altitudeM + climbRateMps * dtSec)
                // Once at cruise altitude, start orbiting automatically.
                val next = if (alt >= cruiseAltitudeM) FlightMode.ORBIT else FlightMode.TAKEOFF
                prev.copy(altitudeM = alt, speedMps = climbRateMps, batteryPct = drained, mode = next)
            }

            FlightMode.ORBIT -> {
                val angle = prev.orbitAngleRad + orbitRateRadPerSec * dtSec
                prev.copy(
                    x = cos(angle) * orbitRadiusM,
                    y = sin(angle) * orbitRadiusM,
                    orbitAngleRad = angle,
                    speedMps = orbitRadiusM * orbitRateRadPerSec,
                    batteryPct = drained,
                )
            }

            FlightMode.RETURN_HOME -> {
                val dist = hypot(prev.x, prev.y)
                if (dist > 0.5f) {
                    // Fly straight toward home at a fixed speed.
                    val move = min(dist, returnSpeedMps * dtSec)
                    prev.copy(
                        x = prev.x - prev.x / dist * move,
                        y = prev.y - prev.y / dist * move,
                        speedMps = returnSpeedMps,
                        batteryPct = drained,
                    )
                } else {
                    // Above home: descend, then land.
                    val alt = max(0f, prev.altitudeM - climbRateMps * dtSec)
                    prev.copy(
                        x = 0f, y = 0f, altitudeM = alt, speedMps = climbRateMps,
                        batteryPct = drained,
                        mode = if (alt == 0f) FlightMode.LANDED else FlightMode.RETURN_HOME,
                    )
                }
            }
        }

        // Safety rule: below the low-battery threshold, abandon the mission and fly home.
        val flying = next.mode == FlightMode.TAKEOFF || next.mode == FlightMode.ORBIT
        return if (flying && next.batteryPct < lowBatteryReturnPct) next.copy(mode = FlightMode.RETURN_HOME) else next
    }
}
