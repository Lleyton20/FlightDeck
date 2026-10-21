package com.lleyton.flightdeck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lleyton.flightdeck.ui.FlightScreen
import com.lleyton.flightdeck.ui.FlightViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FlightScreen(viewModel<FlightViewModel>())
            }
        }
    }
}
