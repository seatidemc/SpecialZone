# SpecialZone

**SpecialZone** 可以设置在某一区域内玩家的行为可能造成的后果。目前仍然处于开发中，且仅支持对死亡不掉落的设定，在将来会添加更多功能。

## Compile

**参考**

- 编写时使用的 Paper API 是 `spigot-api-1.15.2-R0.1-20200410.011959-86.jar`
- 编写时使用的 Java 是 `java 11.0.6 2020-01-14 LTS`

**编译**

本项目使用 gradle，要进行编译，直接 `clone` 后执行 `./gradlew.bat build`（Windows 环境）或 `./gradlew build`（Linux 环境）即可。

**自动编译**

除了手动编译以供测试使用以外，我们使用了 GitHub Actions 进行自动编译。点击[此处](https://github.com/sotapmc/SpecialZone/actions)进入 Actions，选择一个 Build，然后点击 Artifacts 中的 `Package` 下载。例如：https://github.com/sotapmc/SpecialZone/actions/runs/84241648

该链接的最后一位是 Action 的代码。你可以通过在 `bash` 命令行执行如下指令获取最新的 Action 代码：

```bash
curl -sL https://api.github.com/repos/sotapmc/SpecialZone/actions/workflows/gradle.yml/runs  | jq -r '.workflow_runs[0].id?'
```

**Note** `sudo apt install curl jq` required.

## 协议

MIT
