# plasma Live Wallpaper

plasma is a hypnotising live wallpaper for Android 4.1 and newer.

## building 

you need the NDK and Android Studio 2.1 to build this project.

first, compile libplasma.so -- this is a super small native library that draws the effect:

```bash
cd /path/to/android-sdk/ndk-bundle
NDK_PROJECT_PATH=~/src/plasma-android/app/src/main/ ./ndk-build
```

then, compile the app by opening Android Studio and building the project.

## screenshots

![screenshot 1](https://raw.githubusercontent.com/clangen/clangen-projects-static/master/plasma/screenshots/plasma01.png)
![screenshot 2](https://raw.githubusercontent.com/clangen/clangen-projects-static/master/plasma/screenshots/plasma02.png)
![screenshot 3](https://raw.githubusercontent.com/clangen/clangen-projects-static/master/plasma/screenshots/plasma03.png)
![screenshot 4](https://raw.githubusercontent.com/clangen/clangen-projects-static/master/plasma/screenshots/plasma04.png)
![screenshot 5](https://raw.githubusercontent.com/clangen/clangen-projects-static/master/plasma/screenshots/plasma05.png)
![screenshot 6](https://raw.githubusercontent.com/clangen/clangen-projects-static/master/plasma/screenshots/plasma06.png)

