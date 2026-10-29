# Paboki Pocket Option Overlay Assistant

Educational overlay for Pocket Option. It never places trades.

## What it does

- Floating Paboki bubble over other apps
- Tap to expand a 200×150 analysis card
- Screenshot via MediaProjection
- Crop the center 70% chart area
- Read green/red candles and estimate RSI from candle size
- Show **UP** or **DOWN**, confidence **55–75%**, expiry **1 Minute (00:01:00)**
- Bangla voice (`bn-BD`) plus a beep
- Footer: `Educational Only - Paboki.com`

## Build the APK

```bat
flutter pub get
flutter build apk --release
```

The APK is written to `build/app/outputs/flutter-apk/app-release.apk`. Copy it to your landing page as `paboki-pocket.apk`.
