# ![Logo](core/assets-raw/sprites/ui/logotext.png)

[![Build Status](https://github.com/acemany/MindustryV4_reforked/workflows/Tests/badge.svg?event=push)](https://github.com/acemany/MindustryV4_reforked/actions)

This version is going to improve the [mindustry](https://github.com/Anuken/Mindustry) of [V4](https://github.com/Anuken/Mindustry/releases/tag/v63), while not changing the style of the game itself much.

_[Trello Board](https://trello.com/b/IvmwyEwH/mindustry-v4-reforked)_

## Building

If you'd rather compile on your own, follow these instructions.
First, make sure you have Java 8 and JDK 8 installed. Open a terminal in the root directory, and run the following commands:

### Windows

_Running:_ `gradlew desktop:run`  
_Building:_ `gradlew desktop:dist`

### Linux

_Running:_ `./gradlew desktop:run`  
_Building:_ `./gradlew desktop:dist`

### For Server Builds...

Server builds are bundled with each released build (in Releases). If you'd rather compile on your own, replace 'desktop' with 'server' i.e. `gradlew server:dist`.

---

Gradle may take up to several minutes to download files. Be patient. <br>
After building, the output .JAR file should be in `/desktop/build/libs/desktop-release.jar` for desktop builds, and in `/server/build/libs/server-release.jar` for server builds.
