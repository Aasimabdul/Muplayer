# MuPlayer

MuPlayer is a sleek, modern, and open-source local music player for Android, designed with a beautiful dark navy theme and built on Jetpack Compose and AndroidX Media3.

## Features
- **Premium Dark Navy UI:** Clean, responsive, and distraction-free interface.
- **Local Audio Support:** Automatically scans your Android device for local music (MP3, M4A, etc.) using MediaStore.
- **Background Playback:** Seamless playback with lock screen and notification controls powered by MediaSessionService.
- **Favorites & DataStore:** Save your favorite tracks locally without needing a cloud account.
- **Privacy First:** No internet permission required. Your data never leaves your device.
- **No Ads, No Trackers:** 100% Free and Open Source Software (FOSS).

## Requirements
- Android 8.0 (API level 26) or higher.

## Permissions
- `READ_MEDIA_AUDIO` / `READ_EXTERNAL_STORAGE`: Required strictly to scan and play the audio files stored on your device.
- `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_MEDIA_PLAYBACK`: Required to continue playing music and show playback controls when the app is in the background.

## Build Instructions
This project can be built using standard Android Studio or Gradle wrappers. No proprietary secrets or external configuration files are needed.

**To build a debug APK:**
```bash
./gradlew assembleDebug
```
The output APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

**To build a signed release APK:**
You will need to configure your own Keystore inside `build.gradle.kts` (or provide it via environment variables) and run:
```bash
./gradlew assembleRelease
```

## Known Limitations
- "Playlists" and specific "Settings" panels are currently marked as "Coming Soon" in the UI and are placeholders for future development.
- Folder browsing is not yet fully implemented.
- Lyrics reader and Equalizer controls are planned but not yet implemented.

## License
This project is licensed under the [GNU General Public License v3.0](LICENSE). 
You are free to use, modify, and distribute this software under the terms of the GPL-3.0 license.
