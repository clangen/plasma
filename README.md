# plasma Live Wallpaper

plasma is a hypnotising live wallpaper for Android 4.1 and newer.

## building 

you need the NDK and Android Studio 2.1 to build this project.

1. compile libplasma.so -- this is a super small native library that draws the effect. 

```bash
cd /path/to/android-sdk/ndk-bundle
NDK_PROJECT_PATH=~/src/plasma-android/app/src/main/ ./ndk-build
```
2. compile the app by opening Android Studio and building the project.
