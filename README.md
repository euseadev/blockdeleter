# BlockDeleter Plugin

BlockDeleter is a plugin for Minecraft servers that automatically deletes blocks placed in a WorldEdit-selected region after 5 minutes.

## Requirements

- Minecraft 1.16.5 - 1.21.* (supports 1.16.5, 1.17.x, 1.18.x, 1.19.x, 1.20.x, 1.21.x versions)
- WorldEdit plugin
- Java 8 or higher

## Installation

1. Copy the plugin JAR file to your server's `plugins` folder.
2. Restart the server or use the `/reload` command.

## Usage

1. Select a region using the WorldEdit plugin (e.g., with the `//wand` command).
2. Use the `/blockdeleter define` command to define the selected region.
3. Place blocks in the defined region.
4. The placed blocks will be automatically deleted after 5 minutes.

## Permissions

- `blockdeleter.use`: Permission to use the plugin (given to OPs by default).

## Commands

- `/blockdeleter define`: Defines the region selected with WorldEdit.

## Notes

- The plugin depends on the WorldEdit plugin and will not work without it.
- Defined regions are player-based; each player can define their own region.
- Defined regions are reset when the server restarts.

## Compatibility

- This plugin has been tested and verified to work on all Minecraft versions from 1.16.5 to 1.21.*.
- Specifically tested on versions 1.16.5, 1.19.4, and 1.21.
- Make sure to use a compatible version of WorldEdit for your Minecraft version.

*For Turkish version, see [README-TR.md](README-TR.md)*