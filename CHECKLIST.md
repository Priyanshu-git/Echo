# Echo Implementation Checklist

## Legend

- `[x]` Done
- `[~]` In progress / partially scaffolded
- `[ ]` Not started

## Project Foundation

- `[x]` Android app module with Kotlin, Compose, Hilt, Room, DataStore, WorkManager, Media3, and Firestore.
- `[x]` `EchoApplication`, `MainActivity`, launcher resources, and MediaSessionService declaration.
- `[x]` Internet/background playback permissions.
- `[x]` Debug build target configured.

## Data Source

- `[x]` Remove Firebase Storage dependency.
- `[x]` Remove app-side MP3 upload flow.
- `[x]` Replace `storagePath` with `audioUrl`.
- `[x]` Add Firestore-backed `MusicLibraryRepository`.
- `[x]` Keep Room as local cache/state for metadata, favorites, downloads, progress, and local paths.
- `[x]` Use trimmed Firestore `songs` schema.
- `[ ]` Add production `google-services.json`.
- `[ ]` Confirm Firestore rules/App Check decision.

## Playback

- `[x]` Media3 playback controller.
- `[x]` Background MediaSessionService.
- `[x]` Queue, play/pause, next/previous, and seek.
- `[x]` Prefer downloaded `localPath`, otherwise stream `audioUrl`.
- `[~]` Device-test lock-screen controls.

## Downloads

- `[x]` WorkManager-backed individual downloads.
- `[x]` Download from HTTP(S) `audioUrl`.
- `[x]` Save files in app-private storage.
- `[x]` Download All.
- `[x]` Skip already downloaded songs.
- `[x]` Per-song and aggregate progress.
- `[x]` Cancel and delete downloads.
- `[ ]` Add worker tests for success/failure/partial cleanup.

## UI

- `[x]` Library, Search, Now Playing, Downloads, Favorites, Queue, Settings, Storage & Data, About, Song Options, and Download All screens.
- `[x]` Remove Upload Song route and upload entry points.
- `[x]` Update empty states for Firestore-managed library.
- `[x]` Mini-player and reusable song rows.
- `[x]` Light/Dark/System theme support.

## Tests

- `[x]` Search use case test.
- `[x]` SHA-256 utility test.
- `[ ]` Firestore metadata mapping test.
- `[ ]` Local favorite/download preservation test.
- `[ ]` Download All skip/failure/progress test.
- `[ ]` Playback source selection test.
- `[ ]` Queue and settings tests.

## Manual Acceptance

- `[ ]` Browse Firestore-backed library.
- `[ ]` Stream GitHub Release MP3 from `audioUrl`.
- `[ ]` Download individual songs.
- `[ ]` Download all songs.
- `[ ]` Play downloaded songs offline.
- `[ ]` Manage local favorites.
- `[ ]` Manage queue.
- `[ ]` Control playback from lock screen.
- `[ ]` Change theme and primary color.
- `[ ]` Clear downloads and cache.
