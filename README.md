# ReplayNK - MCBE PowerNukkitX camera plugin

![ReplayNK](https://socialify.git.ci/PowerNukkitX/ReplayNK/image?description=1&descriptionEditable=ReplayMod%20for%20MCBE!&font=Inter&forks=1&issues=1&language=1&name=1&owner=1&pattern=Plus&pulls=1&stargazers=1&theme=Light)

##### English | [简体中文](README_zh-cn.md)

`ReplayNK` is a camera plugin developed for the Bedrock Edition server software `PowerNukkitX`, based on the latest Camera API of MCBE 1.20.0

`ReplayNK` has made a lot of optimizations for camera movement, making its camera movement smoother and smoother than the traditional `/teleport`command plus command block to achieve camera movement

`ReplayNK` perfectly supports all available parameters of the original `/camera` command, and adds a series of smoothing algorithms for path smoothing, such as Bézier curves.

`ReplayNK` is simple and intuitive to use, with full visual operation, and most of the functions can be activated with one click of a button. At the same time, the plugin supports Chinese and English dual languages, and will automatically switch according to the user's language settings.

## Simple tutorial

### 1. Install plugin

Download the latest plugin jar package from Github Release, put it into the plugin folder of PowerNukkitX, and restart the server. The resource packs required by the plugin are built into the plugin, and you don't need to install additional resource packs.

After startup, the plugin will generate `plugins/ReplayNK/trails` directory and save trail files in this directory

### 2. Commands

The list of available commands for the plugin is as follows, you can also enter `help replaynk` to get command help

`/replaynk create <name>` - Create a new trail preset

`/replaynk remove <name>` - Delete a new trail preset

`/replaynk operate <name>` - Start operating a trail preset

`/replaynk list` - List all trail presets

### 3. Operation Guide

Here are some things that will appear in your inventory after entering the operation mode (`/replay operate`), we will introduce from left to right

![tools.png](img%2Ftools.png)

`Add Marker` - Add a marker

Add a marker, the marker is the basic unit of the track, you can set the camera position, orientation, camera speed and other parameters on the marker. The newly created marker will inherit the player's position and orientation

`Remove Marker` - Remove a marker

Use this tool to click on a marker to delete the marker point. After deleting a marker, the numbers of all subsequent markers will be reduced by one

`Edit Marker` - Edit a marker

Use this tool to click a marker to edit the parameters of this marker

`Marker Picker` - Pick up a marker

This tool is used to move markers quickly. Left click to select a marker and move to a new position right click, the marker you selected will be moved to your new position

`Play` - Play the trail

Right click to play the trail

`Pause` - Stop playing

Right click on this tool while playing to stop playing

`Setting` - Trail settings

Click this tool to open the trail setting interface, where you can set the smoothing type of the trail, the default camera speed, playback speed and other parameters

`Exit` - Exit edit mode

Right click this tool to exit the edit mode

## Troubleshooting

Q: Why do I feel that the camera is still stuck?

A: Due to the limitation of the client, when the speed of the mirror is too high and the distance between the markers is too small, there will be a freeze phenomenon. We have optimized it as much as possible. You can improve this problem by reducing the camera speed or increasing the marker spacing

Q: Why doesn't my camera reset after playback ends?

A: This may be caused by sending packets too fast. You can fix this by using the command `camera @s clear`

## Related projects

- [PowerNukkitX](https://github.com/PowerNukkitX/PowerNukkitX)