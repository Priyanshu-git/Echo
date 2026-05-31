# Echo Android Architecture Plan

## Product Summary

Echo is a private Android music player for 2-5 users. It supports a shared Firestore-backed music library, streaming from public GitHub Release MP3 asset URLs, offline downloads, local favorites, search, queue management, background playback, lock-screen controls, storage management, and Light/Dark/System theme support.

The app does not upload MP3 files, does not use Firebase Storage, and does not modify GitHub Releases.

## Target Project

- Root: `D:\Code\Android\Echo`
- Package: `com.echo.musicplayer`
- Minimum SDK: 26
- UI: Jetpack Compose + Material 3
- Backend metadata: Firebase Firestore
- MP3 hosting: public GitHub Release asset URLs
- Local persistence: Room + DataStore
- Background work: WorkManager
- Playback: Media3 ExoPlayer + MediaSessionService
- DI: Hilt

## High-Level Architecture

- `domain`: pure Kotlin models, repository interfaces, and use cases.
- `data`: Firestore metadata reads, Room local state/cache, DataStore settings, and repository implementations.
- `download`: WorkManager workers and download orchestration.
- `playback`: Media3 player controller, media session service, lock-screen controls, and queue state.
- `ui`: Compose navigation, screens, reusable components, theme, and app state.

Dependency direction remains `ui -> domain <- data`. Playback and download may depend on `domain`; domain must not depend on Android framework code.

## Song Metadata

Firestore collection: `songs`

Document ID: use `fileHash` when available; otherwise the app falls back to the Firestore document id.

Required shared metadata:

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

Do not store `storagePath`, upload state, favorites, download status, progress, local paths, GitHub credentials, release ids, or asset ids in Firestore.

## Repository Design

- `MusicLibraryRepository`: reads Firestore metadata and merges it with local Room-only state.
- `FavoritesRepository`: local-only favorites.
- `DownloadRepository`: local-only download status/progress and app-private files.
- `PlaybackController`: Media3 playback and queue commands.
- `SettingsRepository`: DataStore-backed app settings.
- `StorageRepository`: local storage usage and cleanup.

Room stores cached metadata plus local-only fields so the UI has one observable library stream.

## Playback And Downloads

Playback source selection:

1. Use `localPath` when the song has been downloaded.
2. Otherwise stream from `audioUrl`.

Downloads:

- Download from public HTTP(S) `audioUrl`.
- Save files in app-private storage as `downloads/<fileHash>.mp3`.
- Skip already downloaded songs.
- Show per-song and aggregate progress.
- Continue Download All when individual songs fail.
- Mark failures locally and remove partial files.

## UI Architecture

Primary tabs:

- Library
- Downloads
- Favorites
- Settings

Secondary routes:

- Search
- Now Playing
- Queue
- Storage & Data
- About
- Song Options
- Download All progress

Upload screens, file pickers, and in-app metadata editing are out of scope unless a separate metadata management tool is added later.

## Testing Strategy

Unit tests cover search, Firestore metadata mapping, local state preservation across refresh, download skip behavior, playback source selection, settings, and queue operations.

Worker/integration tests cover successful HTTP downloads, failed URLs, partial file cleanup, and delete-download behavior.

Manual QA covers Firestore library display, GitHub Release streaming, offline playback, Download All, local favorites, lock-screen controls, theme selection, and storage cleanup.
