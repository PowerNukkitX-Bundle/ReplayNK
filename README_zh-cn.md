# ReplayNK - MCBE PowerNukkitX平台 平滑镜头插件

![ReplayNK](https://socialify.git.ci/PowerNukkitX/ReplayNK/image?description=1&descriptionEditable=ReplayMod%20for%20MCBE!&font=Inter&forks=1&issues=1&language=1&name=1&owner=1&pattern=Plus&pulls=1&stargazers=1&theme=Light)

##### [English](README.md) | 简体中文

`ReplayNK`是为基岩版`PowerNukkitX`平台开发的平滑镜头模组，基于MCBE 1.20.0最新的Camera API开发

`ReplayNK`针对镜头运动进行了大量的优化，使得其运镜的流畅度和平滑度远高于通过传统的`/teleport`指令加命令方块实现的镜头运动

`ReplayNK`完美支持原版`/camera`命令的所有可用参数，并针对路径平滑添加了一系列平滑算法，例如贝塞尔曲线。

`ReplayNK`使用上手简单直观，全可视化操作，大部分功能只需要点击一下按钮就能启用。同时，插件支持中文/英文双语言，会根据用户的语言设置自动切换。

## 简单使用教程

### 1. 安装插件

在Github Release下载最新的插件jar包，放入PowerNukkitX的插件文件夹内，重启服务器即可。插件所需资源包已内置到插件中，你不需要额外安装资源包。

启动后，插件将生成`plugins/ReplayNK/trails`目录并在此目录下保存轨迹文件

### 2. 命令

插件可用命令列表如下，你也可以输入`/help replaynk`来获取命令帮助

`/replaynk create <name>` - 创建一个新的轨迹预设

`/replaynk remove <name>` - 删除一个新的轨迹预设

`/replaynk operate <name>` - 开始操作一个轨迹预设

`/replaynk list` - 列出所有的轨迹预设

### 3. 操作指南

以下是进入操作模式(`/replay operate`)后你的物品栏将会出现的一些东西，我们将从左往右介绍

![tools.png](img%2Ftools.png)

`Add Marker` - 添加标记点

添加一个标记点，标记点是轨迹的基本组成单位，你可以在标记点上设置镜头的位置、朝向、镜头速度等参数。刚创建的标记点会继承玩家的位置和朝向

`Remove Marker` - 删除标记点

使用此工具点击一个标记点，可以删除这个标记点。删除标记点后，其后的所有标记点的编号将会减一

`Edit Marker` - 编辑标记点

使用此工具点击一个标记点，可以编辑这个标记点的参数

`Marker Picker` - 选取标记点

此工具用于快速移动标记点。左键选取标记点并移动到新位置右键，你选取的标记点将会被移动到你所处的新位置

`Play` - 播放

右键开始播放轨迹

`Pause` - 停止播放

在播放时右键此工具可停止播放

`Setting` - 轨迹设置

点击此工具可打开轨迹设置界面，你可以在此界面设置轨迹的平滑类型，默认镜头速度，播放倍速等参数

`Exit` - 退出操作模式

右键此工具可退出编辑模式

## 疑难解答

Q: 为什么我感觉运镜还是很卡顿？

A: 由于客户端限制，在运镜速度过高/标记点间距过小的情况下会出现卡顿现象，我们已经尽量优化了。你可以通过减小镜头速度或者增大标记点间距来改善这个问题

Q: 为什么播放结束了我的镜头却没有复位？

A: 这可能是由于发包过快导致的。你可以通过使用命令`/camera @s clear`来解决此问题

## 相关项目

- [PowerNukkitX](https://github.com/PowerNukkitX/PowerNukkitX)