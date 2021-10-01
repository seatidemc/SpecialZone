# SpecialZone

![](https://img.shields.io/static/v1?label=&message=11&color=darkred&logo=java)
![](https://img.shields.io/static/v1?label=version&message=1.16.5&color=brightgreen)
![Java CI with Gradle](https://github.com/seatidemc/SpecialZone/actions/workflows/gradle.yml/badge.svg)

[点击下载最新构建](https://nightly.link/seatidemc/SpecialZone/workflows/gradle/master/SpecialZone%20latest.zip)

**SpecialZone** 是一个可以在 Minecraft 服务器里划定区域，并对区域的属性进行设置以达到不同的效果的插件。当前支持设置的区域属性包含

- `keepInv` — 在区域内死亡后，是否保留背包物品？
- `keepExp` — 在区域内死亡后，是否保留经验？
- ~~`noBreak` — 是否禁止破坏区域内方块？~~
- `ignoreY` — 是否忽略 Y 轴？

未来会添加更多。

## 不算特点的点

### 无污染

SpecialZone 划定的区域数据存储在 `zones.yml` 中。不需要的时候删除 SpecialZone，不会在服务器中留下任何痕迹。只要 `zones.yml` 存在，所有数据均可以恢复。SpecialZone 监听的事件包括

- 玩家死亡
- 玩家破坏
- 玩家利用羽毛点击方块

（将来可能添加更多）且基本上没有会与其它插件冲突的操作。如果有，请告诉我们，我们会尝试另辟蹊径。

### 简单、易拓展

SpecialZone 的功能很简单，就是将区域的始端和终端的坐标进行记录，然后判断玩家在触发事件时是否在区域内，在哪个区域内，是否需要对此玩家进行特殊处理等。它并不像圈地插件那样有很多可以设置的权限，却有很多（将来会不断添加）可以设置的「区域属性」。

区域属性将根据 SEATiDE 服务器的需求或者灵感逐步添加。在编写拓展时，也只需要考虑相应的事件和处理逻辑。

## 下载

可以在 [Action](https://github.com/seatidemc/SpecialZone/actions) 里下载。

## 协议

MIT
