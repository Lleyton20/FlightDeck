package com.lleyton.flightdeck.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lleyton.flightdeck.data.FlightMode
import com.lleyton.flightdeck.data.Telemetry
import com.lleyton.flightdeck.data.TelemetrySimulator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Everything the screen needs to draw one frame. */
data class FlightUiState(
    val telemetry: Telemetry = Telemetry(),
    val pathXs: List<Float> = emptyList(),
    val pathYs: List<Float> = emptyList(),
)

/**
 * Owns the simulation loop and exposes UI state as a StateFlow.
 * The UI only observes state and sends commands; it never changes state directly.
 */
class FlightViewModel(
    private val simulator: TelemetrySimulator = TelemetrySimulator(),
) : ViewModel() {

    private val _state = MutableStateFlow(FlightUiState())
    val state: StateFlow<FlightUiState> = _state.asStateFlow()

    private val tickMs = 100L
    private val maxPathPoints = 600

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(tickMs)
                _state.update { s ->
                    val t = simulator.step(s.telemetry, tickMs / 1000f)
                    val moving = t.mode != FlightMode.LANDED
                    s.copy(
                        telemetry = t,
                        pathXs = if (moving) (s.pathXs + t.x).takeLast(maxPathPoints) else s.pathXs,
                        pathYs = if (moving) (s.pathYs + t.y).takeLast(maxPathPoints) else s.pathYs,
                    )
                }
            }
        }
    }

    fun takeOff() = setMode(FlightMode.TAKEOFF, onlyFrom = FlightMode.LANDED)
    fun orbit() = setMode(FlightMode.ORBIT, onlyFrom = null)
    fun returnHome() = setMode(FlightMode.RETURN_HOME, onlyFrom = null)

    private fun setMode(mode: FlightMode, onlyFrom: FlightMode?) {
        _state.update { s ->
            val current = s.telemetry.mode
            val allowed = when {
                onlyFrom != null -> current == onlyFrom
                else -> current != FlightMode.LANDED // can't orbit/return while on the ground
            }
            if (allowed) s.copy(telemetry = s.telemetry.copy(mode = mode)) else s
        }
    }
}
