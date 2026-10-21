package com.lleyton.flightdeck.data

/** What the drone is currently doing. */
enum class FlightMode { LANDED, TAKEOFF, ORBIT, RETURN_HOME }

/**
 * One snapshot of the (simulated) drone's state.
 * x/y are meters from the home point; altitude in meters.
 */
data class Telemetry(
    val x: Float = 0f,
    val y: Float = 0f,
    val altitudeM: Float = 0f,
    val speedMps: Float = 0f,
    val batteryPct: Float = 100f,
    val mode: FlightMode = FlightMode.LANDED,
    val orbitAngleRad: Float = 0f,
)
