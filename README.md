# FlightDeck

A simple Android app for flying a **simulated drone** with one-tap commands instead of joysticks.
It shows a live top-down flight path and a heads-up display (altitude, speed, battery, mode).

**Built with:** Kotlin · Jetpack Compose · Android Architecture Components (ViewModel, StateFlow) · C++ via the Android NDK · Python + adb tooling

## How it works

```
TelemetrySimulator (pure Kotlin)  ──►  FlightViewModel (StateFlow)  ──►  FlightScreen (Jetpack Compose)
        ▲ commands: take off / orbit / return home                         │
        └──────────────────────────────────────────────────────────────────┘
                                   Path smoothing ◄── PathMath (JNI) ◄── path_math.cpp (C++)
```

- `data/TelemetrySimulator.kt`: physics of the fake drone (climb, orbit, fly home, land, battery drain, auto return home below 20% battery). No Android code, so it is unit tested.
- `ui/FlightViewModel.kt`: runs the 10 Hz simulation loop and exposes one `StateFlow<FlightUiState>`. The UI only reads state and sends commands.
- `ui/FlightScreen.kt`: Compose UI: HUD, map canvas, command buttons.
- `cpp/path_math.cpp` + `nativebridge/PathMath.kt`: moving-average path smoothing in C++, called through JNI.
- `tools/run_on_device.py`: builds, installs, launches the app and saves a screenshot via adb.
- `tools/check_logs.py`: shows only this app's logs and crashes.

## Run it

1. Open this folder in **Android Studio** (it syncs Gradle and offers to create the Gradle wrapper; accept any version upgrades it suggests).
2. Install the NDK and CMake if prompted (SDK Manager → SDK Tools).
3. Press **Run** on an emulator or phone.
4. Tests: `./gradlew test`

## Build checklist (do these yourself, in order)

- [ ] Day 1: open, sync, run on emulator. Read each file and explain it in one sentence in `NOTES.md`.
- [ ] Run the unit tests; add one test of your own (e.g., battery never goes below 0).
- [x] Add a **low-battery auto return home** rule (e.g., under 20%) and a test for it.
- [ ] Add a **waypoint mission**: tap the map to add points; the drone visits them in order.
- [ ] Draw a **direction arrow** on the drone dot using its heading.
- [ ] (Stretch) Render the path in **OpenGL ES** instead of Canvas.
- [ ] Record a screen GIF for this README.
