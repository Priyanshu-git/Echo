# Echo Android Implementation Plan

## Current Direction

Echo uses Firestore for shared song metadata and public GitHub Release asset URLs for MP3 streaming/downloads. The app is Android-only and private for 2-5 users.

In-app MP3 upload is removed. Firebase Storage is not part of the implementation.

## Implemented Architecture

- Domain model uses `Song.audioUrl` for the remote MP3 source.
- Firestore `songs` documents provide shared metadata.
- Room stores the merged app library plus local-only state: favorites, download status, progress, and `localPath`.
- Media3 streams from `audioUrl` or plays downloaded `localPath`.
- WorkManager downloads `audioUrl` into app-private storage.
- DataStore persists app settings.

## Firestore Schema

```json
{
  "title": "",
  "artist": "",
  "album": "",
  "durationMs": 0,
  "audioUrl": "",
  "coverArtUrl": "",
  "fileName": "",
  "sizeBytes": 0,
  "updatedAt": "",
  "fileHash": ""
}
```

Use `fileHash` as the stable id when present; otherwise use the Firestore document id.

## Remaining Implementation Work

- Add `google-services.json` for the target Firebase project.
- Apply Google Services Gradle plugin if required by the chosen Firebase setup.
- Confirm Firestore rules for private metadata access.
- Device-test background playback and lock-screen controls.
- Add repository/worker tests for Firestore refresh, local state preservation, HTTP downloads, failures, and Download All behavior.

## Acceptance Criteria

- Library displays songs from Firestore metadata.
- Undownloaded songs stream from GitHub Release `audioUrl`.
- Downloaded songs play offline from local app-private storage.
- Download All skips already downloaded songs and continues after individual failures.
- Favorites and download status remain local only.
- No app-side upload flow, Firebase Storage dependency, or `storagePath` field remains.
