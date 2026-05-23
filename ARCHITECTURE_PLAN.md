# Echo Android Architecture Plan

## Product Summary
Echo is a private Android music player for a small group of users. It supports a shared music library, MP3 uploads, streaming, offline downloads, favorites, search, queue management, background playback, lock-screen controls, storage management, and a modern dark-only UI with a persistent dynamic primary color.

## Target Project
- Root: `D:\Code\Android\Echo`
- Package: `com.echo.musicplayer`
- Minimum SDK: 26
- UI: Jetpack Compose + Material 3
- Presentation: MVI with ViewModels as state containers
- Backend: Firebase Firestore + Firebase Storage
- Local persistence: Room + DataStore
- Background work: WorkManager
- Playback: Media3 ExoPlayer + MediaSessionService
- DI: Hilt

## High-Level Architecture
Use a single Android app module with clean internal boundaries:

- `core`: shared utilities, result wrappers, dispatchers, permissions, hashing, metadata helpers, formatters.
- `domain`: pure Kotlin models, repository interfaces, and use cases.
- `data`: Firebase, Room, DataStore, DTO/entity mapping, and repository implementations.
- `playback`: Media3 player controller, media session service, notification and lock-screen behavior, queue state.
- `download`: WorkManager workers and download orchestration.
- `ui`: Compose navigation, screens, reusable components, theme, and MVI contracts.

Dependency direction should remain:

`ui -> domain <- data`

`playback` and `download` may depend on `domain`, but domain must not depend on Android framework code.

## MVI Presentation Pattern
Each screen should define:

- `State`: immutable state required to render the screen.
- `Intent`: user and system actions.
- `Effect`: one-time events such as navigation, snackbars, file picker launches, and permission prompts.
- `ViewModel`: receives intents, calls use cases, reduces state, and emits effects.

Use `StateFlow` for screen state and `SharedFlow` or `Channel` for one-time effects.

## Core Domain Models
Primary models:

- `Song`
- `SongMetadataDraft`
- `DownloadStatus`
- `PlaybackState`
- `QueueItem` if queue metadata grows beyond `Song`
- `AppSettings`
- `StorageUsage`

Required song fields:

```json
{
  "title": "",
  "artist": "",
  "album": "",
  "durationMs": 0,
  "storagePath": "",
  "fileName": "",
  "sizeBytes": 0,
  "uploadedAt": "",
  "updatedAt": "",
  "fileHash": ""
}
```

## Repository Interfaces
Create domain-level interfaces for:

- `MusicLibraryRepository`
- `UploadRepository`
- `FavoritesRepository`
- `DownloadRepository`
- `PlaybackController`
- `SettingsRepository`
- `StorageRepository`

Repositories should expose observable streams for app state and suspend functions for commands.

## Firebase Design
Firestore:

- Collection: `songs`
- Document ID: preferably `fileHash`
- Stores song metadata only.

Firebase Storage:

- MP3 files: `songs/{fileHash}.mp3`
- Optional artwork cache: `artwork/{fileHash}.jpg`

Duplicate prevention:

- Compute SHA-256 before upload.
- Check Firestore for existing `fileHash`.
- Block duplicate uploads before sending file bytes.

Security assumption:

- V1 has no login because auth is out of scope.
- For real private deployment, Firebase rules and App Check must be configured before distribution.

## Local Storage Design
Room stores:

- Cached song metadata.
- Download status.
- Local file paths.
- Device-specific favorites.

DataStore stores:

- Primary color.
- Download-over-Wi-Fi-only setting.
- Keep-screen-on-while-playing setting.

Downloaded files:

- Store in app-private storage.
- Do not require broad storage permissions on modern Android.

## Playback Architecture
Use Media3:

- `ExoPlayer` for streaming and offline files.
- `MediaSessionService` for background playback and lock-screen controls.
- A `PlaybackController` abstraction for UI and use cases.
- Queue state exposed as a `Flow<PlaybackState>`.

Playback must continue while:

- App is backgrounded.
- Device is locked.
- User navigates between screens.

## Download Architecture
Use WorkManager for resilient downloads:

- Individual song downloads run as workers.
- Download All creates coordinated work for pending songs.
- Already downloaded songs are skipped.
- Failed individual songs do not stop the entire batch.
- Aggregate progress is visible.
- Cancellation is supported.

## UI Architecture
Top-level navigation:

- Library
- Downloads
- Favorites
- Settings

Secondary routes:

- Search
- Now Playing
- Upload Song
- Queue
- Storage & Data
- About
- Song Options sheet
- Download All progress

UI direction:

- Dark-only Material 3.
- Clean modern lists with compact song rows.
- Cover art thumbnail with placeholder fallback.
- Persistent mini-player on primary tabs.
- Dynamic primary color applied immediately across controls, tabs, progress, toggles, and playback actions.

## Testing Strategy
Unit tests:

- MVI reducers and ViewModel state transitions.
- Search matching by title, artist, album.
- Upload validation.
- SHA-256 hash generation.
- Duplicate detection flow.
- Favorite persistence behavior.
- Download All skip/failure/cancel/progress behavior.
- Settings persistence.
- Queue operations.

Manual acceptance:

- Upload MP3 and edit metadata.
- Browse and search library.
- Stream song.
- Download song and play offline.
- Download all with cancellation.
- Manage favorites.
- Manage queue.
- Use lock-screen playback controls.
- Change primary color instantly.
