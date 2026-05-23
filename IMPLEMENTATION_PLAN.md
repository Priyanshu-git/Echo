# Echo Android Implementation Plan

## Phase 1: Project Foundation
Create the Android project at `D:\Code\Android\Echo`.

Tasks:

- Add Gradle wrapper and Android Gradle project files.
- Configure app module with Kotlin, Compose, Hilt, Room, DataStore, WorkManager, Media3, and Firebase dependencies.
- Add package `com.echo.musicplayer`.
- Add `EchoApplication` with Hilt.
- Add `MainActivity` with Compose entry point.
- Add Android manifest permissions and MediaSessionService declaration.
- Add dark-only base theme and app resources.

Acceptance:

- `:app:assembleDebug` starts dependency resolution and compilation.
- App has a valid launcher activity.

## Phase 2: Domain Layer
Implement pure domain types and contracts.

Tasks:

- Add models: `Song`, `SongMetadataDraft`, `DownloadStatus`, `PlaybackState`, `AppSettings`, `StorageUsage`.
- Add repository interfaces for library, upload, favorites, downloads, settings, storage, and playback.
- Add use cases for search, upload validation, favorite toggle, download orchestration, and queue commands.
- Add utilities for duration formatting, file size formatting, and SHA-256 hashing.

Acceptance:

- Domain layer has no Android framework dependency.
- Search and validation can be unit tested independently.

## Phase 3: Data Layer
Implement local-first repositories with Firebase-ready boundaries.

Tasks:

- Add Room database entities for songs, favorites, and downloads.
- Add DataStore settings repository.
- Add Firebase-backed library repository shell.
- Add Firebase upload repository shell with metadata extraction and duplicate detection hooks.
- Add download repository backed by WorkManager and local file state.
- Add storage repository for downloaded/cache/other usage.
- Add mapper functions between Firebase DTOs, Room entities, and domain models.

Acceptance:

- App can render sample/local song data before Firebase is configured.
- Repository interfaces can be swapped from sample implementation to real Firebase implementation without UI changes.

## Phase 4: Playback Layer
Implement Media3 playback integration.

Tasks:

- Add `EchoMediaSessionService`.
- Add `Media3PlaybackController` implementing `PlaybackController`.
- Support play, pause, next, previous, seek, queue reorder, queue remove, and queue clear.
- Support streaming source URLs and local downloaded file paths.
- Add lock-screen playback controls through MediaSession.
- Keep playback state observable for Compose screens.

Acceptance:

- Starting playback from Library updates mini-player and Now Playing state.
- Playback continues in background.
- Lock-screen controls appear on supported devices.

## Phase 5: Download Layer
Implement individual download and Download All behavior.

Tasks:

- Add `DownloadSongWorker`.
- Add per-song progress state.
- Add Download All orchestration.
- Skip already downloaded songs.
- Continue when individual downloads fail.
- Support cancel, retry, and delete downloaded song.
- Store completed files in app-private storage.

Acceptance:

- Downloads screen shows all, downloaded, downloading, failed states.
- Download All shows aggregate progress and current item.
- Downloaded songs play offline.

## Phase 6: Upload Flow
Implement MP3 upload flow.

Tasks:

- Launch Android file picker for MP3 selection.
- Extract title, artist, album, duration, file size, and embedded artwork.
- Show editable metadata draft screen.
- Validate required fields.
- Compute SHA-256.
- Prevent duplicates.
- Upload file to Firebase Storage.
- Save metadata to Firestore.
- Refresh shared library after upload.

Acceptance:

- User can select an MP3, review/edit metadata, upload it, and see it in the Library.

## Phase 7: Compose UI And Navigation
Build screens using MVI.

UI direction:

- The supplied screenshot is a reference for product coverage and general tone, not an exact visual specification.
- Use modern, smooth, clean dark-only UI patterns that support the planned features well.
- Prefer clarity, compact music-player ergonomics, responsive touch targets, and consistent app-wide primary color usage over pixel matching the reference image.

Tasks:

- Add Compose navigation graph.
- Add bottom navigation for Library, Downloads, Favorites, Settings.
- Add Library screen with song list, search action, upload action, and mini-player.
- Add Search screen with dynamic filtering.
- Add Now Playing screen with artwork, metadata, seek bar, playback controls, favorite, download, and queue actions.
- Add Upload Song screen.
- Add Downloads screen with tabs and Download All.
- Add Favorites screen.
- Add Queue screen with reorder/remove/clear.
- Add Settings screen with primary color selector and toggles.
- Add Storage & Data screen.
- Add About screen.
- Add song options sheet.

Acceptance:

- All planned screens are reachable.
- UI is modern, dark, clean, and usable on phone screens.
- Dynamic primary color changes immediately.

## Immediate Next Steps

The app currently has a compiling, sample-backed Compose shell. Continue from here in this order:

1. Polish the current dark UI for real app ergonomics: spacing, hierarchy, empty states, bottom mini-player behavior, navigation transitions, and screen consistency.
2. Replace in-memory settings with DataStore so primary color and toggles persist.
3. Add Room entities, DAOs, and local repositories for songs, favorites, downloads, and cached library state.
4. Replace in-memory playback with Media3 `ExoPlayer` and a real `MediaSessionService`.
5. Add WorkManager-backed individual downloads and Download All orchestration.
6. Add Android MP3 picker, metadata extraction, editable upload draft state, SHA-256 duplicate checks, and upload progress.
7. Wire Firebase Firestore/Storage after local behavior is stable.
8. Add focused unit tests for search, validation, settings persistence, queue operations, and download orchestration.

## Phase 8: Firebase Configuration
Wire real Firebase after the app shell compiles.

Tasks:

- Add `google-services.json`.
- Apply Google Services Gradle plugin.
- Configure Firestore and Storage references.
- Replace sample repository behavior with real Firebase reads/writes.
- Add Firebase rules documentation.
- Add App Check if required.

Acceptance:

- Shared library reflects Firestore data.
- Upload writes to Storage and Firestore.
- Streaming uses Firebase Storage URLs.

## Phase 9: Testing And Hardening
Add test coverage and manual QA.

Tasks:

- Add unit tests for use cases and reducers.
- Add ViewModel tests for main MVI flows.
- Add repository tests with fakes.
- Add manual QA checklist for playback, uploads, downloads, queue, favorites, settings, and storage.
- Run build and unit tests.
- Fix compile, lint, and runtime issues.

Acceptance:

- Debug build succeeds.
- Unit tests pass.
- Major BRD acceptance criteria are verified manually.
