# Voxy NeoForge移植设计文档

## 概述

将Voxy LoD渲染模组从Fabric移植到NeoForge 1.21.1，采用多平台模块架构，同时整合Fakesight的区块请求欺骗功能。

## 项目信息

| 属性 | 值 |
|------|-----|
| 模组名称 | Voxy |
| Mod ID | voxy |
| 包名 | me.cortex.voxy |
| 版本 | 0.2.14-alpha (继承) |
| 目标版本 | Minecraft 1.21.1 NeoForge |
| 渲染依赖 | Sodium + Iris |

## 架构设计

### 目录结构

```
voxy/
├── common/                     # 平台无关核心代码
│   ├── src/main/java/me/cortex/voxy/
│   │   ├── common/             # 世界引擎、存储、线程服务
│   │   ├── commonImpl/         # 实例管理
│   │   ├── client/             # 客户端核心（GL渲染、模型烘焙）
│   │   └── api/                # 平台抽象接口（新增）
│   └── src/main/resources/
│       ├── assets/voxy/
│       └── voxy-common.mixins.json
│
├── fabric/                     # Fabric平台实现（原有）
│   ├── src/main/java/me/cortex/voxy/fabric/
│   │   ├── VoxyFabricMod.java
│   │   └── platform/
│   └── src/main/resources/
│       ├── fabric.mod.json
│       └ voxy.accesswidener
│       └── voxy-fabric.mixins.json
│
├── neoforge/                   # NeoForge平台实现（新增）
│   ├── src/main/java/me/cortex/voxy/neoforge/
│   │   ├── VoxyNeoForgeMod.java      # 主入口
│   │   ├── platform/                 # 平台实现
│   │   │   ├── NeoForgePlatform.java
│   │   │   ├── NeoForgeConfig.java
│   │   │   └ NeoForgeCommands.java
│   │   ├── mixin/                    # NeoForge特定Mixin
│   │   │   ├── minecraft/
│   │   │   ├── sodium/
│   │   │   └── iris/
│   │   └── fakesight/                # 整合的Fakesight功能
│   │       ├── IntegratedServerMixin.java
│   │       └ OptionsMixin.java
│   ├── src/main/resources/
│   │   ├── META-INF/
│   │   │   ├── neoforge.mods.toml
│   │   │   └ accesstransformer.cfg
│   │   └── voxy-neoforge.mixins.json
│
├── build.gradle
├── settings.gradle
└ gradle.properties
```

### 平台抽象接口

```java
// common/api/IPlatform.java
public interface IPlatform {
    boolean isModLoaded(String modId);
    Path getConfigDir();
    Path getGameDir();
    EnvType getEnvironmentType();
    String getModVersion();
    Optional<Path> getModRootPath();
    
    // 静态访问方法
    static IPlatform getInstance() { return INSTANCE; }
    static void setInstance(IPlatform instance) { INSTANCE = instance; }
}
```

## 核心代码转换

### Fabric到NeoForge转换表

| Fabric代码 | NeoForge等效 |
|-----------|-------------|
| `FabricLoader.getInstance().isModLoaded()` | `ModList.get().isLoaded()` 或 `Platform.isModLoaded()` |
| `FabricLoader.getInstance().getConfigDir()` | `FMLPaths.CONFIGDIR.get()` |
| `ClientModInitializer` | `@Mod(dist = Dist.CLIENT)` |
| `ClientCommandRegistrationCallback` | `RegisterCommandsEvent` |
| `fabric.mod.json` | `neoforge.mods.toml` |
| `accessWidener` | `accesstransformer.cfg` |

### NeoForge入口类

```java
@Mod(value = "voxy", dist = Dist.CLIENT)
public class VoxyNeoForgeMod {
    public VoxyNeoForgeMod(IEventBus bus, ModContainer container) {
        // 1. 设置平台实例
        Platform.setInstance(new NeoForgePlatform(container));
        
        // 2. 注册配置界面（使用Sodium配置系统）
        container.registerExtensionPoint(IConfigScreenFactory.class, 
            (mc, screen) -> VoxyConfigScreenPages.createScreen(screen));
        
        // 3. 注册命令
        bus.addListener(RegisterCommandsEvent.class, this::registerCommands);
        
        // 4. 初始化渲染系统工厂
        VoxyCommon.setInstanceFactory(VoxyClientInstance::new);
        
        // 5. 注册事件监听
        bus.addListener(ClientTickEvent.Post.class, this::onClientTick);
    }
    
    private void registerCommands(RegisterCommandsEvent event) {
        VoxyCommands.register(event.getDispatcher());
    }
}
```

## Fakesight功能整合

### 配置扩展

在VoxyConfig中新增Fakesight相关配置：

```java
public class VoxyConfig {
    // 原有配置
    public boolean enabled = true;
    public boolean enableRendering = true;
    public boolean ingestEnabled = true;
    public float sectionRenderDistance = 16;
    public int serviceThreads = 4;
    public float subDivisionSize = 64;
    public boolean renderVanillaFog = true;
    
    // 新增：Fakesight区块请求欺骗
    public boolean enableExtendedRequestDistance = true;
    public int requestDistance = 48;  // 服务器请求距离（区块）
}
```

### Mixin实现

**IntegratedServerMixin.java**:
```java
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @ModifyArg(
        method = "tickServer",
        at = @At(
            value = "INVOKE",
            target = "Ljava/lang/Math;max(II)I",
            ordinal = 0
        ),
        index = 1
    )
    private int voxy$modifyRenderDistance(int originalDistance) {
        if (VoxyConfig.CONFIG.enableExtendedRequestDistance) {
            return VoxyConfig.CONFIG.requestDistance;
        }
        return originalDistance;
    }
}
```

**OptionsMixin.java**:
```java
@Mixin(Options.class)
public abstract class OptionsMixin {
    @Redirect(
        method = "buildPlayerInformation",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;",
            ordinal = 0
        )
    )
    private Object voxy$modifyPlayerInfoRenderDistance(OptionInstance<?> instance) {
        if (VoxyConfig.CONFIG.enableExtendedRequestDistance) {
            return Integer.valueOf(VoxyConfig.CONFIG.requestDistance);
        }
        return instance.get();
    }
}
```

### 配置界面集成

在Voxy配置界面添加新选项组：

- **扩展区块请求设置**
  - 启用扩展区块请求：开关
  - 请求距离：滑块 (8-127区块)

## Mixin系统

### Access Transformer配置

将`voxy.accesswidener`转换为`accesstransformer.cfg`：

```properties
# 类访问
public net.minecraft.client.multiplayer.ClientChunkCache$Storage
public com.mojang.blaze3d.platform.GlDebug$LogEntry

# 字段访问
public net.minecraft.client.multiplayer.ClientLevel levelRenderer
public net.minecraft.client.color.block.BlockColors blockColors
public net.minecraft.client.renderer.texture.TextureAtlas mipLevel
public-f net.minecraft.world.level.chunk.PalettedContainer$Data palette
public-f net.minecraft.world.level.chunk.PalettedContainer$Data storage

# 方法访问
public net.minecraft.client.renderer.GameRenderer getFov(Lnet/minecraft/client/Camera;FZ)D
public net.minecraft.client.multiplayer.ClientChunkCache$Storage getChunk(I)Lnet/minecraft/world/level/chunk/LevelChunk
public net.minecraft.client.multiplayer.ClientChunkCache$Storage getIndex(II)I
```

### Mixin配置

**voxy-common.mixins.json**（平台无关Mixin）:
```json
{
  "package": "me.cortex.voxy.mixin",
  "required": true,
  "compatibilityLevel": "JAVA_21",
  "injectors": { "defaultRequire": 1 },
  "client": [
    "minecraft.MixinLevelRenderer",
    "minecraft.MixinClientLevel"
  ]
}
```

**voxy-neoforge.mixins.json**（NeoForge特定）:
```json
{
  "package": "me.cortex.voxy.neoforge.mixin",
  "required": true,
  "compatibilityLevel": "JAVA_21",
  "injectors": { "defaultRequire": 1 },
  "overwrites": { "conformVisibility": true },
  "client": [
    "minecraft.MixinClientChunkCache",
    "sodium.MixinSodiumWorldRenderer",
    "sodium.MixinRenderSectionManager",
    "iris.MixinIrisRenderingPipeline",
    "fakesight.IntegratedServerMixin",
    "fakesight.OptionsMixin"
  ]
}
```

## Gradle构建系统

### settings.gradle
```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://maven.neoforged.net/releases' }
    }
}

rootProject.name = 'voxy'
include 'common', 'fabric', 'neoforge'
```

### gradle.properties
```properties
org.gradle.jvmargs=-Xmx2G
minecraft_version=1.21.1
mod_version=0.2.14-alpha
maven_group=me.cortex
archives_base_name=voxy

# Fabric
fabric_loader_version=0.17.2
fabric_api_version=0.116.6+1.21.1

# NeoForge
neoforge_version=21.1.82

# 依赖
sodium_version=mc1.21.1-0.6.13
iris_version=1.8.8+1.21.1
lithium_version=mc1.21.1-0.15.0
```

### neoforge/build.gradle.kts
```kotlin
plugins {
    id("net.neoforged.moddev") version("2.0.42-beta")
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.shedaniel.me/")
}

dependencies {
    implementation(project(":common"))
    implementation("maven.modrinth:sodium:$sodium_version-neoforge")
    implementation("maven.modrinth:iris:$iris_version-neoforge")
    implementation("maven.modrinth:lithium:$lithium_version-neoforge")
    
    // 原生库
    jarJar("org.lwjgl:lwjgl-lmdb:3.3.3")
    jarJar("org.lwjgl:lwjgl-zstd:3.3.3")
    jarJar("org.rocksdb:rocksdbjni:10.2.1")
    jarJar("redis.clients:jedis:5.1.0")
    jarJar("org.apache.commons:commons-pool2:2.12.0")
    jarJar("org.lz4:lz4-java:1.8.0")
}

neoForge {
    version = "21.1.82"
    accessTransformers { 
        file("src/main/resources/META-INF/accesstransformer.cfg") 
    }
    runs {
        create("Client") { client() }
    }
}
```

## 模组元数据

### neoforge.mods.toml
```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "All-Rights-Reserved"

[[mods]]
modId = "voxy"
version = "${version}"
displayName = "Voxy"
description = '远距离LoD渲染模组，使用层次化区块数据提供超远距离渲染'

[[dependencies.voxy]]
modId = "minecraft"
type = "required"
versionRange = "[1.21.1]"
side = "CLIENT"

[[dependencies.voxy]]
modId = "neoforge"
type = "required"
versionRange = "[21.1.82,)"
side = "CLIENT"

[[dependencies.voxy]]
modId = "sodium"
type = "required"
versionRange = "[0.6.13,)"
side = "CLIENT"

[[dependencies.voxy]]
modId = "iris"
type = "optional"
ordering = "AFTER"
side = "CLIENT"

[[dependencies.voxy]]
modId = "embeddium"
type = "incompatible"
reason = "Voxy需要原生Sodium，不兼容Embeddium"
side = "CLIENT"

[[mixins]]
config = "voxy-common.mixins.json"

[[mixins]]
config = "voxy-neoforge.mixins.json"
```

## 存储系统

保持原有存储后端：
- **LMDB**: 高性能键值存储（默认）
- **RocksDB**: 可选替代存储
- **Redis**: 分布式存储选项
- **内存存储**: 测试/临时使用

## 命令系统

使用NeoForge命令API重写：

```java
public class VoxyCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("voxy")
            .then(Commands.literal("reload")
                .executes(ctx -> reloadConfig(ctx)))
            .then(Commands.literal("debug")
                .executes(ctx -> toggleDebug(ctx)))
            .then(Commands.literal("import")
                .then(Commands.argument("path", StringArgumentType.string())
                    .executes(ctx -> importData(ctx))))
            .then(Commands.literal("distance")
                .then(Commands.argument("blocks", IntegerArgumentType.integer(8, 127))
                    .executes(ctx -> setRequestDistance(ctx))))
        );
    }
}
```

## 测试计划

1. **编译测试**: 确保项目可编译
2. **基础功能测试**: 
   - 模组加载
   - 配置界面
   - 命令系统
3. **渲染功能测试**:
   - LOD渲染正常工作
   - Sodium集成正常
   - Iris shader兼容
4. **Fakesight功能测试**:
   - 区块请求距离欺骗生效
   - 服务器正确接收请求
5. **存储测试**:
   - LMDB存储正常
   - RocksDB存储正常
   - 数据持久化

## 风险评估

| 风险 | 影响 | 解决方案 |
|------|------|---------|
| Sodium API变化 | 高 | 参考Sodium NeoForge实现 |
| Iris shader兼容 | 中 | 使用NeoForge版Iris |
| Mixin冲突 | 中 | 使用Mixin插件动态加载 |
| 原生库加载 | 低 | NeoForge支持jarJar嵌入 |

## 预计工作量

| 任务 | 预计时间 |
|------|---------|
| 项目结构搭建 | 1天 |
| 平台抽象层 | 2天 |
| NeoForge入口和配置 | 1天 |
| Mixin系统转换 | 2天 |
| Fakesight整合 | 1天 |
| 命令系统重写 | 1天 |
| 测试和调试 | 3天 |
| **总计** | **~10天** |