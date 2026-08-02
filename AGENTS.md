# AGENTS.md

## Project
BeatPlay — Bengali live-TV & movie streaming Android app (`com.beat.play`).
Firebase Realtime DB, Material3 Dark theme, Media3 ExoPlayer.

## User's Standing Rules (MUST follow)

1. **Git push after every completed update**: Every time a round of code
   updates is complete and verified (build/lint/static checks pass), commit
   and push to GitHub (`origin`, repo: `DrSamual/BeatPlay`) **with a version
   bump + git tag**. Do this only when everything is fine.
2. Version convention: bump `versionCode`/`versionName` in `app/build.gradle`
   (currently 1 / "1.0"). Tag commits like `v1.x.y`.
3. Work in Bengali UI strings; keep existing design system
   (colors/cards/gradient) consistent.

## Key technical facts
- minSdk 21, target/compileSdk 34, AGP 8.5.2, Gradle 8.7, JDK 17
- Dependencies: appcompat, material, constraintlayout, recyclerview, cardview,
  drawerlayout, viewpager2, core, media3-exoplayer(+hls/dash/rtsp/smoothstreaming),
  media3-ui, glide, firebase-bom + firebase-database
- `app/google-services.json` is gitignored and currently a **placeholder** —
  user must replace with their real Firebase config before release.
- DataStore refs: `channels()`, `movies()`, `settings()`, `banners()`, `notifications()`
- Colors: colorPrimary #1A1A2E, colorAccent #E94560, cardBg #222244, textPrimary #F5F5FA ...
- `PlayerActivity.start(Context, title, url)` is the entry API used by many screens.
- YouTube links play via WebView fallback; all other links via ExoPlayer.
- Admin playlist import supports URL / paste / file-manager (SAF OpenDocument).

## Build environment (already set up in this machine)
- JDK 17: `/usr/bin/java`
- Gradle 8.7: `/opt/gradle/gradle-8.7/bin/gradle`
- Android cmdline-tools: `/tmp/opencode/android-sdk/cmdline-tools/latest/bin`
- SDK components (platform-34, build-tools) still need install, then:
  `ANDROID_HOME=/tmp/opencode/android-sdk` + `sdkmanager --licenses`
  then `gradle assembleDebug` → APK at `app/build/outputs/apk/debug/app-debug.apk`
