# MuPlayer - Major Upgrade Release Notes

Welcome to a massive new update for MuPlayer! We've overhauled the user interface with a premium, dynamic design, added highly requested audio features, and squashed numerous bugs to ensure your music listening experience is smoother and more beautiful than ever.

## ✨ What's New
* **Stunning "Liquid Glass" UI Redesign:** The entire app has been refreshed with a premium "Liquid Glass" aesthetic. Menus, popups, dialogs, and cards now feature beautiful translucent backgrounds, soft highlights, and gentle shadows that dynamically adapt to all 10 custom color themes!
* **Floating Dynamic Mini-Player:** The mini-player on the main screens has been completely redesigned. It now elegantly hovers above the bottom navigation bar like a pill, complete with an animated progress line along its bottom edge, and smoothly glides in and out when music starts or stops.
* **5-Band Custom Equalizer:** Take full control of your audio! Inside the Equalizer menu, you can now select a "Custom" preset which opens 5 precise frequency sliders (60Hz to 14kHz), allowing you to fine-tune your bass, mids, and treble exactly how you like.
* **Animated Waveform Timeline:** The playback progress bar on the Now Playing screen now features a dynamic, animated sine wave that pulses to the music when playing and flattens out perfectly when paused or scrubbing.
* **M3U Playlist Imports:** You can now import your `.m3u` and `.m3u8` playlist files directly into the app! Just tap the new "Import" button on the Playlists tab to browse your files.
* **Library Multi-Select Mode:** Long-press any song in the Library (Songs) tab to enter selection mode. You can now effortlessly batch-delete multiple songs from your device or add dozens of songs to a playlist with a single click!
* **Smart "Most Played" & "Liked Songs" Playlists:** The Playlists tab now automatically generates smart virtual playlists at the very top for your "Liked Songs" and "Most Played Songs", making your favorite tracks instantly accessible.
* **Repeat Button in Notifications:** You can now toggle your Repeat settings (Off / Repeat One / Repeat All) directly from your Android lock screen and notification shade!

## 🐛 Bug Fixes & Improvements
* **Volume Slider Stability:** Fixed a severe bug where the UI volume slider would violently jump or drop to 0% during automatic song crossfades. The slider is now rock solid and only moves when you manually drag it or use your hardware buttons.
* **Accurate Play Counts:** Fixed an issue where background song transitions weren't accurately counting towards your play history. The "Most Played" rankings are now 100% accurate and strictly sorted.
* **Crossfade & Sleep Timer Fixes:** Fixed audio glitches where the player would unexpectedly jump to 100% volume right before fading out at the end of a sleep timer. Volume memory is now perfectly preserved.
* **Dynamic Widget Coloring:** Home screen widgets now smartly extract the "Muted" color from your current album art to tint their backgrounds, keeping them legible while matching the vibe of your music.
* **Permissions Handling:** Fixed an issue on Android 13+ where the app would fail to ask for Notification permissions on the first launch.
* General performance optimizations, removal of deprecated Jetpack Compose code, and memory leak preventions.

Enjoy the music! 🎵
