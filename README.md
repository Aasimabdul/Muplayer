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
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

For a locally installable release variant, run `./gradlew assembleRelease`. This repository does not configure a production signing key, so configure your own secure signing setup before distributing a signed production APK.

## Release verification

The GitHub Actions workflow runs unit tests and assembles the debug APK. A tagged release is published only after the release workflow's build and unit-test step succeeds. UI behavior still needs testing on real Android devices, particularly permissions, audio effects, media notifications, and touch interactions.

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE).
