#!/usr/bin/env python3
"""Build, install, launch FlightDeck on a connected device/emulator, then save a screenshot.

Usage: python3 tools/run_on_device.py [--no-build]
Needs: adb on PATH, a device/emulator connected (check with `adb devices`).
"""
import argparse
import datetime
import pathlib
import subprocess
import sys
import time

ROOT = pathlib.Path(__file__).resolve().parent.parent
PACKAGE = "com.lleyton.flightdeck"
GRADLEW = ROOT / ("gradlew.bat" if sys.platform.startswith("win") else "gradlew")


def run(cmd, **kw):
    print("$", " ".join(map(str, cmd)))
    return subprocess.run(cmd, check=True, **kw)


def main():
    p = argparse.ArgumentParser()
    p.add_argument("--no-build", action="store_true", help="skip the Gradle build")
    args = p.parse_args()

    if not args.no_build:
        run([str(GRADLEW), "installDebug"], cwd=ROOT)
    run(["adb", "shell", "monkey", "-p", PACKAGE, "-c", "android.intent.category.LAUNCHER", "1"])
    time.sleep(3)

    out_dir = ROOT / "screenshots"
    out_dir.mkdir(exist_ok=True)
    out = out_dir / f"flightdeck_{datetime.datetime.now():%Y%m%d_%H%M%S}.png"
    with open(out, "wb") as f:
        run(["adb", "exec-out", "screencap", "-p"], stdout=f)
    print("Saved", out)


if __name__ == "__main__":
    main()
