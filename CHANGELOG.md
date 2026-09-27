# Changelog

All notable changes to **90 FPS BOOSTER** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2026-09-27
### 🚀 90 FPS Stability & Frame Pacing Engine

### Added
- **Dual-Lock Refresh Rate Governor**: Locks both `min_refresh_rate` and `peak_refresh_rate` to `90.0` (and `user_refresh_rate`) via Shizuku, eliminating Android's dynamic display frequency hopping and touch-inactivity stutters.
- **Stutter & 1% Low Telemetry**: Real-time measurement of 1% low FPS, frame delivery jitter (ms), dropped janks, and stability index (%).
- **Android Game Mode (Performance)**: Seamless integration with Android 12+ Game Mode API (`cmd game mode 2 <package>`) via Shizuku.
- **Stability Advisory & Diagnostics**: In-depth recommendations for GPU rendering budgets (setting in-game graphics to "Smooth" while keeping FPS at "90 / Extreme").
- **In-App GitHub Releases Page**: Dedicated GitHub release tracker, changelog viewer, and update checker directly within the app.

### Changed
- Enhanced `OptimizationEngine` with synchronized min/max refresh rate locking.
- Upgraded `PerformanceSampler` with Choreographer ring buffer for statistical frame time distribution.

---

## [1.0.0] - 2026-09-26
### 🎉 Initial Production Release

### Added
- **Esports Gaming Dashboard**: High-contrast dark charcoal theme (`#0B0D11`) with electric yellow accents (`#FFE600`) and animated radial gauges.
- **Official Shizuku Integration**: Complete integration of `dev.rikka.shizuku:api` and `dev.rikka.shizuku:provider` with zero root requirements.
- **Dynamic Hardware Detection**: Real-time detection of display panel capabilities, MediaTek Helio G88 / ARM Mali-G52 MC2 GPU, RAM, and internal storage.
- **Display Limit Honesty**: Explicitly detects 60 Hz display panels (e.g. Infinix XPad 20 standard edition) and prevents misleading 90 Hz claims.
- **Automated Game Detection Service**: Foreground service with notification controls to apply profiles on game launch and restore defaults on exit.
- **Thermal Safety System**: Continuous monitoring via Android's `PowerManager` thermal status listener, pausing overrides during severe thermal throttling (≥ 42°C).
- **Activity Log & Undo**: Full Room database audit trail tracking all applied settings with one-tap undo capability.
- **Per-App Profile Manager**: Custom profiles for individual installed games and apps.
