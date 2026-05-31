# Echo

Echo is a private Android-only offline-capable music player for a small group of users. It uses Kotlin, Jetpack Compose, Hilt, Firestore, Room, DataStore, WorkManager, and Media3.

## Current State

The app reads shared song metadata from the Firestore `songs` collection and uses public GitHub Release asset URLs in `audioUrl` for streaming and downloads. Downloaded songs, favorites, progress, and local file paths stay on-device only.

MP3 upload from the app is intentionally not supported. MP3 files are uploaded manually to GitHub Releases, and song metadata is added or managed separately in Firestore.

## Firestore Song Schema

```json
{
  "title": "",
  "artist": "",
  "album": "",
  "durationMs": 0,
  "audioUrl": "",
  "fileName": "",
  "sizeBytes": 0,
  "updatedAt": "",
  "fileHash": ""
}
```

## Build

```powershell
.\gradlew.bat :app:assembleDebug
```
