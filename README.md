# Voxy NeoForge Port

**LoD (Level of Detail) 远距离渲染模组 - NeoForge移植版**

## 项目说明

本项目是将 Voxy LoD渲染模组从Fabric移植到NeoForge 1.21.1的版本，同时整合了 Fakesight 区块请求欺骗功能。

## 作者信息

| 角色 | 作者 | GitHub |
|------|------|--------|
| **Voxy原作者** | CortexMC | https://github.com/CortexMC/Voxy |
| **Voxy原作者** | m3t4f1v3 | https://github.com/m3t4f1v3/voxy |
| **Fakesight作者** | MoePus | https://github.com/MoePus/fakesight |
| **NeoForge移植** | paperfrog | - |

## 原版项目地址

- **Voxy (CortexMC)**: https://github.com/CortexMC/Voxy
- **Voxy (m3t4f1v3)**: https://github.com/m3t4f1v3/voxy
- **Fakesight (MoePus)**: https://github.com/MoePus/fakesight

## 主要特性

- **多模块架构**: common/fabric/neoforge三模块分离
- **LoD渲染**: 远距离区块使用简化几何体渲染，大幅提升渲染距离
- **Fakesight功能**: 内置区块请求欺骗，向服务器请求更远区块数据
- **完整存储后端**: 支持LMDB、RocksDB、Redis存储
- **Sodium配置集成**: 配置选项集成到Sodium视频设置界面

## 技术细节

- 基于Voxy原版代码进行NeoForge适配
- **不依赖Forgified Fabric API**（Sodium/Iris自带所需模块）
- 依赖Sodium和Iris进行渲染优化
- 使用Sodium配置系统进行设置管理
- 使用ModDevGradle构建系统

## 配置选项

### Voxy渲染设置

| 选项 | 默认值 | 说明 |
|------|--------|------|
| 启用Voxy | true | 启用/禁用整个模组 |
| Voxy渲染 | true | 启用/禁用LoD渲染 |
| 服务线程数 | CPU核心/1.5 | 后台处理线程数 |
| 区块渲染距离 | 128 | LoD渲染范围（区块） |
| 细分像素大小 | 64 | 屏幕空间细分阈值 |

### Fakesight区块请求

| 选项 | 默认值 | 说明 |
|------|--------|------|
| 启用扩展区块请求 | true | 向服务器请求更远区块 |
| 请求距离 | 48 | 服务器区块请求距离 |

### 调试选项

| 选项 | 默认值 | 说明 |
|------|--------|------|
| 隐藏初始化警告 | false | 隐藏存储配置初始化警告 |

## 依赖

- **NeoForge**: 21.1.122+ (推荐 21.1.216)
- **Sodium**: 0.6.13+mc1.21.1
- **Iris**: 1.8.8+mc1.21.1 (可选，用于shader支持)

## 不兼容

- **Embeddium**: 需要原生Sodium
- **Optifine**: 与Sodium不兼容

## 构建

```bash
# 设置Java 21环境
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.10.7-hotspot"

# 构建
./gradlew build

# 输出文件
neoforge/build/libs/voxy-neoforge-0.2.14-alpha.jar
```

## 安装

将构建的jar文件放入NeoForge整合包的`mods`文件夹即可。

## 免责声明

- 本项目为移植版本，可能存在与原版行为差异
- 使用本模组请确保服务器允许扩展区块请求（Fakesight功能）
- 性能表现取决于硬件配置和世界复杂度
- 原版问题请反馈到原作者仓库，移植问题可反馈到此仓库

## 许可证

本项目遵循原Voxy项目的许可证（All-Rights-Reserved）。

## 致谢

- **CortexMC** - Voxy和Fakesight原作者
- **Sodium团队** - [CaffeineMC/sodium](https://github.com/CaffeineMC/sodium)
- **Iris团队** - [IrisShaders/Iris](https://github.com/IrisShaders/Iris)
- **NeoForge团队** - 提供优秀的模组加载器