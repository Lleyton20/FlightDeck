package com.lleyton.flightdeck

import com.lleyton.flightdeck.data.FlightMode
import com.lleyton.flightdeck.data.Telemetry
import com.lleyton.flightdeck.data.TelemetrySimulator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TelemetrySimulatorTest {
    private val sim = TelemetrySimulator()

    private fun run(start: Telemetry, seconds: Float, dt: Float = 0.1f): Telemetry {
        var t = start
        repeat((seconds / dt).toInt()) { t = sim.step(t, dt) }
        return t
    }

    @Test fun landedDroneDoesNotMoveOrDrainBattery() {
        val t = run(Telemetry(), seconds = 10f)
        assertEquals(0f, t.altitudeM, 0.001f)
        assertEquals(100f, t.batteryPct, 0.001f)
    }

    @Test fun takeoffClimbsToCruiseThenOrbits() {
        val t = run(Telemetry(mode = FlightMode.TAKEOFF), seconds = 20f)
        assertEquals(30f, t.altitudeM, 0.001f)
        assertEquals(FlightMode.ORBIT, t.mode)
    }

    @Test fun flyingDrainsBattery() {
        val t = run(Telemetry(mode = FlightMode.TAKEOFF), seconds = 10f)
        assertTrue(t.batteryPct < 100f)
    }

    @Test fun returnHomeEndsLandedAtOrigin() {
        val start = Telemetry(x = 50f, y = 0f, altitudeM = 30f, mode = FlightMode.RETURN_HOME)
        val t = run(start, seconds = 40f)
        assertEquals(FlightMode.LANDED, t.mode)
        assertEquals(0f, t.x, 0.001f)
        assertEquals(0f, t.altitudeM, 0.001f)
    }

    @Test fun lowBatteryTriggersReturnHome() {
        val start = Telemetry(x = 50f, altitudeM = 30f, batteryPct = 20.01f, mode = FlightMode.ORBIT)
        val t = run(start, seconds = 1f)
        assertEquals(FlightMode.RETURN_HOME, t.mode)
    }

    @Test fun lowBatteryDuringTakeoffReturnsHomeAndLands() {
        val start = Telemetry(altitudeM = 5f, batteryPct = 19f, mode = FlightMode.TAKEOFF)
        val t = run(start, seconds = 30f)
        assertEquals(FlightMode.LANDED, t.mode)
        assertEquals(0f, t.altitudeM, 0.001f)
    }

    @Test fun batteryNeverGoesBelowZero() {
        val start = Telemetry(x = 50f, altitudeM = 30f, batteryPct = 0.1f, mode = FlightMode.RETURN_HOME)
        val t = run(start, seconds = 60f)
        assertTrue(t.batteryPct >= 0f)
    }
}
