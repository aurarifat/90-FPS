# 90 FPS BOOSTER

A native, production-quality Android utility built with Kotlin, Jetpack Compose, Material 3, and the official Shizuku API. The app dynamically manages supported Android performance settings, display refresh rate preferences, per-game profiles, and real-time telemetry using Shizuku and Android Wireless Debugging where authorized.

---

## ⚠️ Realistic Performance & Hardware Transparency

- **No False Promises**: 90 FPS BOOSTER never promises, fakes, or simulates stable 90 FPS. Actual gaming frame rate depends strictly on hardware limits, thermal conditions, GPU throttling, and native game engine capabilities.
- **Display Refresh Rate vs. Game Render FPS**: Requesting a high refresh rate (e.g., 90 Hz or 120 Hz) configures the physical screen's display scanout frequency. **It does not force or trick a game's 3D rendering engine to produce 90 frames per second** if the game caps itself at 30 or 60 FPS or if the GPU cannot render that fast.
- **Physical Panel Bottlenecks (Target Device: Infinix XPad 20)**:
  - If the device (such as standard Infinix XPad 20 models) possesses a **60 Hz physical display panel**, the app dynamically detects this via `Display.supportedModes` and flags 90 Hz display modes as physically unsupported by the hardware panel.
  - If a device panel supports 90 Hz or 120 Hz, those options are dynamically unlocked in the profile editor.
- **Thermal Safety**: Android's thermal manager actively steps in when the device exceeds safe operating temperatures. This utility automatically pauses aggressive optimizations during severe thermal warnings to safeguard battery health and avoid thermal shutdown.

---

## 🚀 Key Features

1. **Esports Gaming Dashboard**:
   - Dark charcoal gaming UI (`#0B0D11`) with vibrant electric yellow accents (`#FFE600`).
   - Animated dual arc gauges for Display Refresh Rate and Render Frame Timing.
   - Real-time hardware telemetry: CPU load, RAM usage, Battery level, and Battery temperature.
   - Quick one-tap "Request Max Supported Rate", "Restore Defaults", and "Undo Last Action".

2. **Per-App Game Profile Manager**:
   - Discover and choose installed games and apps using Android's `PackageManager`.
   - Configure custom Target FPS (Auto, 30, 60, 90, 120) and display refresh rate preferences.
   - Per-app toggle for automatic activation upon launch.
   - Optional pre-launch memory cache trim.

3. **Official Shizuku & Wireless Debugging Integration**:
   - Direct integration using `dev.rikka.shizuku:api` and `dev.rikka.shizuku:provider`.
   - Never requests or assumes root access.
   - Safe whitelist-checked execution of supported Android display refresh rate settings (`settings put system min_refresh_rate`, `peak_refresh_rate`, `user_refresh_rate`).
   - Built-in step-by-step Wireless Debugging pairing guide with one-tap shortcuts to Android Developer Options.

4. **Background Game Detection Service**:
   - Lightweight Android Foreground Service with low-priority status notification.
   - Detects foreground game launches and automatically applies saved profiles.
   - Automatically restores standard system display refresh rates when the game closes.
   - Quick "Restore Defaults" and "Stop" actions directly from the notification shade.

5. **Diagnostic Compatibility Checker & Activity Audit Log**:
   - Analyzes display panel refresh rates, SoC/GPU architecture (e.g. MediaTek Helio G88 / Mali-G52 MC2), and Shizuku status.
   - Safe optimization checklist: booster memory cache clearance, thermal headroom advice, and battery saver status.
   - Complete Room database audit log tracking all profile activations, timestamps, previous/new values, and undo support.

---

## 🛠 Shizuku Wireless Debugging Setup Guide (Android 11+)

1. **Enable Developer Options**:
   - Go to **Android Settings > About Phone**.
   - Tap **Build Number** 7 times until you see "You are now a developer!".
2. **Connect to Wi-Fi**:
   - Wireless Debugging requires an active local Wi-Fi connection.
3. **Turn on Wireless Debugging**:
   - In **Settings > System > Developer Options**, toggle ON **Wireless Debugging**.
   - Tap into **Wireless Debugging** and tap **Pair device with pairing code**.
4. **Pair with Shizuku**:
   - Open the **Shizuku** app (or tap the pairing notification).
   - Enter the 6-digit Wi-Fi pairing code and port.
   - Return to Shizuku's main screen and tap **Start**.
5. **Authorize in 90 FPS BOOSTER**:
   - Open **90 FPS BOOSTER**, navigate to **Settings**, and tap **Authorize**.
   - Grant the one-time Shizuku permission prompt.

---

## 🏗 Build & Run

### Prerequisites
- Android Studio Ladybug / Meerkat or Gradle 8.11+
- Android SDK 36 (compileSdk 36, minSdk 24)
- JDK 17 or JDK 21

### Building the APK
```bash
# Debug build
gradle :app:assembleDebug

# Run unit tests
gradle :app:testDebugUnitTest
```

---

## 📱 Hardware Architecture & Target Device Specifications

- **Infinix XPad 20 Specifications**:
  - **SoC**: MediaTek Helio G88 (MT6769)
  - **CPU**: 2x Cortex-A75 @ 2.0 GHz + 6x Cortex-A55 @ 1.8 GHz
  - **GPU**: ARM Mali-G52 MC2
  - **Memory**: 6 GB LPDDR4X RAM / 128 GB Storage
  - **Display Panel**: Dynamically tested via Android DisplayManager. If the hardware panel is 60 Hz, the app explicitly communicates that 90 Hz display output is physically impossible on the panel.
