# MuPlayer V.6.2.0 🎵

MuPlayer 6.2.0 focuses on smoother navigation and reducing unnecessary playback-resume preference writes.

## Added and improved

- Added restrained horizontal slide-and-fade transitions between navigation destinations, with separate timings for forward and back navigation.
- Kept navigation transitions lightweight, using short alpha and horizontal-offset animations rather than animated layout sizing or full-screen blur.
- Reduced playback-resume SharedPreferences writes from every 500 ms to track changes and a five-second interval while preserving the existing progress update cadence.
- Preserved the existing application ID, Compose UI, Media3 playback service, tab destinations, and mini-player architecture.

## Existing playback features

- Local music playback with background service and media notification controls.
- Queue restoration, repeat, shuffle, playlists, favorites, and library scanning.
- Equalizer presets, custom EQ, bass boost where supported, lyrics, sleep timer, themes, and home-screen widgets.

## Verification status

The source changes require CI/build verification and on-device testing before this version is suitable for production distribution. No frame-time benchmark or physical-device playback test is claimed by these notes.

## Requirements

- Android 8.0 (API 26) or newer.
