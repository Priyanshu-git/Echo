# Echo

Echo is a private offline-capable Android music player scaffolded from the BRD. It uses Kotlin, Jetpack Compose, MVI, Firebase-ready repositories, Media3, Room, DataStore, WorkManager, and Hilt.

## Current state

This is an architecture-first implementation with working navigation, dark UI, MVI screen contracts, local sample data, and integration shells for Firebase uploads/downloads and Media3 playback. Add `google-services.json` and tighten Firebase rules before using a real shared library.

## Build

```powershell
.\gradlew.bat :app:assembleDebug
```
