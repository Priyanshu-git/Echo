# Echo Implementation Checklist

## Legend
- `[x]` Done
- `[~]` In progress / partially scaffolded
- `[ ]` Not started

## Project Foundation
- `[x]` Create target folder `D:\Code\Android\Echo`.
- `[x]` Add Gradle wrapper and base Gradle files.
- `[x]` Configure Android app module dependencies.
- `[x]` Add package `com.echo.musicplayer`.
- `[x]` Add `EchoApplication` and `MainActivity`.
- `[~]` Add manifest permissions and MediaSessionService declaration.
- `[x]` Add dark app theme resources.
- `[x]` Add launcher icons.
- `[x]` Confirm debug build compiles.

## Architecture Skeleton
- `[x]` Add `core` package.
- `[x]` Add `domain` package.
- `[~]` Add `data` package.
- `[ ]` Add `download` package.
- `[~]` Add `playback` package.
- `[x]` Add `ui` package with navigation and screens.
- `[x]` Add Hilt modules and dependency bindings.

## Domain Layer
- `[x]` Add core domain models.
- `[x]` Add repository interfaces.
- `[x]` Add search use case.
- `[x]` Add upload validation use case.
- `[x]` Add formatting and hash utilities.
- `[ ]` Add queue-specific use cases.
- `[ ]` Add download orchestration use cases.

## Data Layer
- `[ ]` Add Room database entities and DAOs.
- `[ ]` Add Room database provider.
- `[ ]` Add DataStore settings implementation.
- `[ ]` Add Firebase library repository.
- `[ ]` Add Firebase upload repository.
- `[ ]` Add local favorites repository.
- `[ ]` Add download repository.
- `[ ]` Add storage usage repository.
- `[ ]` Add mapping functions.
- `[x]` Add sample/fake repositories for local development.

## Playback
- `[~]` Add Media3 `EchoMediaSessionService`.
- `[ ]` Add `Media3PlaybackController`.
- `[~]` Support play/pause/resume.
- `[~]` Support next/previous.
- `[~]` Support seek.
- `[~]` Support queue state.
- `[ ]` Support background playback.
- `[ ]` Support lock-screen controls.
- `[ ]` Support streaming and local playback sources.

## Downloads
- `[ ]` Add `DownloadSongWorker`.
- `[~]` Download individual songs.
- `[~]` Delete downloaded songs.
- `[ ]` Retry failed downloads.
- `[~]` Download all songs.
- `[~]` Skip already downloaded songs.
- `[ ]` Continue when individual downloads fail.
- `[~]` Cancel active batch.
- `[~]` Show aggregate progress.

## Uploads
- `[ ]` Launch Android MP3 picker.
- `[ ]` Extract MP3 metadata.
- `[ ]` Extract embedded cover art.
- `[~]` Show metadata review/edit form.
- `[~]` Validate required fields.
- `[ ]` Compute SHA-256 file hash.
- `[ ]` Block duplicate uploads.
- `[ ]` Upload MP3 to Firebase Storage.
- `[ ]` Save metadata to Firestore.
- `[~]` Refresh library after upload.

## UI And MVI
- `[x]` Confirm screenshot is a directional reference, not a pixel-perfect requirement.
- `[~]` Keep UI modern, smooth, clean, dark-only, and aligned with planned features.
- `[~]` Add shared MVI contracts.
- `[x]` Add app navigation graph.
- `[x]` Add reusable song row.
- `[x]` Add reusable artwork placeholder.
- `[x]` Add mini-player.
- `[x]` Add Library screen.
- `[x]` Add Search screen.
- `[x]` Add Now Playing screen.
- `[x]` Add Upload Song screen.
- `[x]` Add Downloads screen.
- `[x]` Add Favorites screen.
- `[x]` Add Queue screen.
- `[x]` Add Settings screen.
- `[x]` Add Storage & Data screen.
- `[x]` Add About screen.
- `[x]` Add Song Options sheet.
- `[x]` Add Download All progress UI.

## Theme And Settings
- `[x]` Choose dark-only theme direction.
- `[x]` Add Material 3 dark color scheme.
- `[x]` Add dynamic primary color selector.
- `[ ]` Persist selected primary color.
- `[x]` Apply primary color across app instantly.
- `[~]` Add Wi-Fi-only download setting.
- `[~]` Add keep-screen-on setting.

## Firebase
- `[ ]` Add Google Services plugin.
- `[ ]` Add `google-services.json`.
- `[ ]` Configure Firestore `songs` collection.
- `[ ]` Configure Firebase Storage paths.
- `[ ]` Document Firebase rules.
- `[ ]` Add App Check decision.

## Tests
- `[ ]` Test SHA-256 hash utility.
- `[ ]` Test search by title, artist, and album.
- `[ ]` Test upload validation.
- `[ ]` Test duplicate detection behavior.
- `[ ]` Test favorites persistence behavior.
- `[ ]` Test Download All skip/failure/cancel/progress behavior.
- `[ ]` Test settings persistence.
- `[ ]` Test queue operations.
- `[ ]` Test MVI reducers/ViewModels.

## Manual Acceptance
- `[ ]` Upload MP3 and edit metadata.
- `[~]` Browse shared library.
- `[~]` Search songs.
- `[ ]` Stream songs.
- `[~]` Download individual songs.
- `[~]` Download all songs.
- `[ ]` Play offline.
- `[~]` Manage favorites.
- `[~]` Manage queue.
- `[ ]` Control playback from lock screen.
- `[~]` Change primary color instantly.
- `[~]` Clear downloads and cache.

## Current Progress Note
The app now builds a debug APK with a dark, sample-backed Compose UI, Hilt bindings, in-memory repositories, navigation, and all planned screens. The screenshot is a feature/tone reference rather than an exact UI requirement; future UI work should prioritize a modern, smooth, clean dark experience. Items marked `[~]` are present as scaffolded or in-memory behavior and still need production backing such as Room, DataStore persistence, WorkManager, Media3 playback, Android file picking, and Firebase.

## Next Implementation Steps
1. Polish the sample-backed Compose UI for consistency, empty states, touch ergonomics, and smooth navigation.
2. Implement DataStore-backed settings persistence for primary color and toggles.
3. Add Room-backed local songs, favorites, downloads, and cache state.
4. Replace in-memory playback with Media3 playback and lock-screen controls.
5. Add WorkManager downloads and the real Android MP3 upload flow.
6. Wire Firebase once the local app behavior is stable.
