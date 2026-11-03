# MuPlayer

MuPlayer is a privacy-first Android music player for local audio, built with Kotlin, Jetpack Compose, AndroidX Media3, Room, and DataStore.

## Features

- **Local music library:** Scan and play audio stored on the device.
- **Background playback:** Media session, notification and lock-screen controls.
- **Playback controls:** Seek, shuffle, repeat, and independent in-app volume.
- **Library tools:** Search, sorting, favorites, multi-select, and tag editing.
- **Playlists:** Create and manage playlists, smart Liked Songs / Most Played lists, and import M3U/M3U8 files.
- **Audio tools:** Equalizer presets, custom 5-band EQ, and bass boost where the device supports them.
- **Personalization:** Ten color themes, optional Liquid Glass styling, and animation settings.
- **Lyrics and sleep timer:** Import local lyrics and schedule playback to stop.
- **Home-screen widgets:** Playback controls and track information.
- **Privacy-first design:** No account is required; music and app preferences are stored locally. The app manifest does not request the Internet permission.

## Requirements

- Android 8.0 (API 26) or newer.
- Audio access permission to scan local music.
- Notification permission on Android 13+ for playback notifications.

## Build and test

Use JDK 17 and the included Gradle wrapper:

```bash
bash ./gradlew testDebugUnitTest lintDebug assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

A production release must be signed with the established project keystore. The release build reads these Gradle properties (never commit their values):

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

## GitHub Actions and production releases

The Android CI workflow runs unit tests, Android lint, and a debug assembly for pull requests and development branches. Its debug APK is a CI artifact only and is not a production release.

The production release workflow refuses to publish unless all of these repository Actions secrets are configured using the project's authorized signing keystore:

- `ANDROID_KEYSTORE_BASE64` — base64-encoded keystore file
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

Push a version tag matching the Gradle `versionName` (for example, `V.6.2.0`) only after tests pass and the correct signing key is available. The workflow builds `assembleRelease`, verifies the APK signature, publishes the APK and a SHA-256 checksum, and refuses to overwrite an existing release. Do not use a debug APK for production distribution.

UI behavior still needs testing on real Android devices, particularly permissions, audio effects, media notifications, reduced-motion behavior, and touch interactions.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
