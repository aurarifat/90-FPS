# 90 FPS BOOSTER v1.1.0 — Stable Release

🚀 **The ultimate gaming performance utility and refresh rate manager with official Shizuku Wireless Debugging integration.**

---

### 🌟 What's New in v1.1.0
- **90 FPS Frame Pacing Stabilizer**:
  - Implements synchronized **Dual-Rate Lock** (`min_refresh_rate` and `peak_refresh_rate` fixed to `90.0`).
  - Eliminates Android's touch-inactivity refresh rate fluctuations (where the display drops from 90Hz to 60Hz mid-match).
- **Real-Time Stutter & 1% Lows Telemetry**:
  - New Choreographer frame timing ring buffer tracking **Stability Index (%)**, **1% Low FPS**, **Jank counts**, and **Delivery Jitter (±ms)**.
- **Android Game Mode (Performance)**:
  - Supports Android 12+ Game Mode API overrides via Shizuku (`cmd game mode 2 <package>`).
- **Thermal Safety Governor**:
  - Automatically pauses aggressive overrides when battery temp hits 42°C to prevent thermal throttling damage.
- **In-App GitHub Release Page & Update Hub**:
  - Built-in GitHub Releases browser, changelog, and update checker.

---

### 📦 Assets & Downloads
- `90FPSBooster-v1.1.0.apk`: Full production application package (~23 MB).
- `90FPSBooster-v1.1.0.apk.sha256`: SHA-256 verification hash for APK.
- `90FPSBooster-v1.1.0-source-code.zip`: Complete Source Code archive bundle (~712 KB).
- `90FPSBooster-v1.1.0-source-code.zip.sha256`: SHA-256 verification hash for Source Code.

---

### 🔧 Requirements
- Android 7.0+ (API 24+)
- **Shizuku** (v13.0+) via Android Wireless Debugging (No root required!)
- Target Device: Infinix XPad 20 (Helio G88 / 6GB RAM) and all high-refresh-rate Android devices.
