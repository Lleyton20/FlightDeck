#!/usr/bin/env python3
"""Stream only FlightDeck's logs and crashes from the device (filters adb logcat)."""
import subprocess

PACKAGE = "com.lleyton.flightdeck"

pid = subprocess.run(["adb", "shell", "pidof", PACKAGE], capture_output=True, text=True).stdout.strip()
cmd = ["adb", "logcat"] + (["--pid", pid] if pid else ["*:E"])
print("Streaming logs (Ctrl+C to stop):", " ".join(cmd))
subprocess.run(cmd)
