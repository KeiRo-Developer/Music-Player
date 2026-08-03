# Resonance — Android Music Player

A stylish, dark-themed local music player built with Kotlin + Jetpack Compose.

## Features

- **FLAC + lossless support** — playback runs on Media3/ExoPlayer, which decodes
  FLAC, WAV, and ALAC natively (also MP3, AAC, OGG). No third-party codec needed.
- **Lossless badges** — tracks in FLAC/WAV/ALAC get a gold "lossless" pill in the
  library and now-playing screen.
- **Synced lyrics** — drop a `.lrc` file next to a song (same filename, e.g.
  `Song Title.flac` + `Song Title.lrc`) and the now-playing screen auto-scrolls
  and highlights the current line in time with playback. Falls back to a plain
  `.txt` lyric file, or a friendly empty state if neither exists.
- **Stylish now-playing UI** — blurred, darkened album art as an ambient
  backdrop, gradient play button, animated transport controls.
- **Background playback** — a Media3 `MediaSessionService` keeps music playing
  with full lock-screen / notification / Bluetooth controls.

## Project structure

```
app/src/main/java/com/keiro/musicplayer/
  data/            Song model, MediaStore library scanner
  lyrics/          .lrc parser + synced-line lookup
  player/          Media3 PlaybackService (background playback)
  viewmodel/       PlayerViewModel — bridges Compose UI to MediaController
  ui/theme/        Colors, typography, dark Material3 theme
  ui/components/   Reusable bits (equalizer bars, format badge)
  ui/screens/      LibraryScreen, NowPlayingScreen
  MainActivity.kt  Permission request + navigation
  MusicApp.kt      Application class
```

## How to build

1. Install **Android Studio** (Koala or newer) if you don't have it.
2. Open this folder as a project (`File > Open`, select the `MusicPlayer` folder).
3. Let Gradle sync — it will pull in Compose, Media3/ExoPlayer, and Coil.
4. Run on a device or emulator running **Android 8.0 (API 26) or later**.
5. On first launch, grant the audio permission prompt so the app can scan
   your music library.

## Adding lyrics to your library

For any song file, add a `.lrc` file with the same base name in the same
folder:

```
/Music/Artist/Song Title.flac
/Music/Artist/Song Title.lrc   <- synced lyrics, standard [mm:ss.xx] format
```

Example `.lrc` content:
```
[00:12.50]First line of the song
[00:16.90]Second line
[00:21.30]Third line
```

## Notes / next steps you may want

- This is a solid, working foundation, not a polished commercial app — you'll
  want to test on-device, and possibly add: playlists, search, a queue/
  shuffle/repeat UI, embedded lyric/metadata reading (e.g. via a tagging lib
  for ID3/Vorbis comments), and a proper launcher icon/splash screen.
- Album art currently comes from `MediaStore`'s embedded-art cache; if a FLAC
  file's embedded art isn't indexed by MediaStore on some OEM skins, you may
  want to fall back to reading embedded FLAC picture blocks directly.
