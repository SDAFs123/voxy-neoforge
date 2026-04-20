# Voxy NeoForge移植 - 项目结构搭建计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建多模块Gradle项目结构，分离common、fabric、neoforge模块

**Architecture:** 参考 Sodium 的多模块架构，common模块包含平台无关代码，fabric/neoforge模块包含平台特定实现

**Tech Stack:** Gradle 8.x, NeoGradle/ModDevGradle, Fabric Loom

---

## Chunk 1: Gradle配置文件创建

### Task 1: 创建根级Gradle配置

**Files:**
- Create: `settings.gradle`
- Create: `gradle.properties`
- Create: `build.gradle`
- Modify: 无

- [ ] **Step 1: 创建settings.gradle**

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://maven.neoforged.net/releases' }
        maven { url 'https://maven.fabricmc.net' }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
        maven { url 'https://maven.neoforged.net/releases' }
        maven { url 'https://maven.fabricmc.net' }
        maven { url 'https://api.modrinth.com/maven' }
        maven { url 'https://maven.shedaniel.me/' }
        maven { url 'https://maven.terraformersmc.com/releases/' }
    }
}

rootProject.name = 'voxy'
include 'common'
include 'fabric'
include 'neoforge'
```

- [ ] **Step 2: 创建gradle.properties**

```properties
org.gradle.jvmargs=-Xmx2G
org.gradle.caching=true
org.gradle.parallel=true

# Minecraft版本
minecraft_version=1.21.1

# 模组信息
mod_version=0.2.14-alpha
maven_group=me.cortex
archives_base_name=voxy

# Fabric版本
fabric_loader_version=0.17.2
fabric_api_version=0.116.6+1.21.1
loom_version=1.11-SNAPSHOT

# NeoForge版本
neoforge_version=21.1.82

# Sodium版本
sodium_version=mc1.21.1-0.6.13

# Iris版本
iris_version=1.8.8+1.21.1

# Lithium版本
lithium_version=mc1.21.1-0.15.0

# 原生库版本
lwjgl_version=3.3.3
rocksdb_version=10.2.1
jedis_version=5.1.0
commons_pool2_version=2.12.0
lz4_version=1.8.0
```

- [ ] **Step 3: 创建根build.gradle**

```groovy
plugins {
    id 'base'
}

subprojects {
    group = project.maven_group
    version = project.mod_version

    tasks.withType(JavaCompile).configureEach {
        options.encoding = 'UTF-8'
        options.release = 21
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }
}
```

- [ ] **Step 4: 创建目录结构**

Run: 创建模块目录
```powershell
mkdir -p common/src/main/java/me/cortex/voxy
mkdir -p common/src/main/resources
mkdir -p fabric/src/main/java/me/cortex/voxy/fabric
mkdir -p fabric/src/main/resources
mkdir -p neoforge/src/main/java/me/cortex/voxy/neoforge
mkdir -p neoforge/src/main/resources/META-INF
```

Expected: 目录结构创建成功

- [ ] **Step 5: 验证Gradle项目配置**

Run: `gradlew projects`
Expected: 显示三个子项目 (common, fabric, neoforge)

---

## Chunk 2: Common模块配置

### Task 2: 创建common模块Gradle配置

**Files:**
- Create: `common/build.gradle`
- Create: `common/src/main/java/me/cortex/voxy/api/package-info.java`

- [ ] **Step 1: 创建common/build.gradle**

```groovy
plugins {
    id 'java-library'
}

repositories {
    mavenCentral()
    maven { url 'https://maven.neoforged.net/releases' }
    maven { url 'https://maven.fabricmc.net' }
}

dependencies {
    // Minecraft (通过NeoForge/Fabric提供，这里仅声明)
    compileOnly "com.mojang:minecraft:${project.minecraft_version}"
    
    // 原生库 - common模块需要
    api "org.lwjgl:lwjgl-lmdb:${project.lwjgl_version}"
    api "org.lwjgl:lwjgl-zstd:${project.lwjgl_version}"
    api "org.rocksdb:rocksdbjni:${project.rocksdb_version}"
    api "redis.clients:jedis:${project.jedis_version}"
    api "org.apache.commons:commons-pool2:${project.commons_pool2_version}"
    api "org.lz4:lz4-java:${project.lz4_version}"
    
    // FastUtil
    api 'it.unimi.dsi:fastutil:8.5.15'
    
    // JOML
    api 'org.joml:joml:1.10.5'
}

// 配置输出配置，供其他模块消费
configurations {
    create('commonApiJava') { canBeConsumed = true; canBeResolved = false }
    create('commonApiResources') { canBeConsumed = true; canBeResolved = false }
    create('commonMainJava') { canBeConsumed = true; canBeResolved = false }
    create('commonMainResources') { canBeConsumed = true; canBeResolved = false }
}

artifacts {
    commonApiJava(sourceSets.main.output.classesDirs)
    commonApiResources(sourceSets.main.output.resourcesDir)
    commonMainJava(sourceSets.main.output.classesDirs)
    commonMainResources(sourceSets.main.output.resourcesDir)
}

java {
    withSourcesJar()
}
```

- [ ] **Step 2: 创建api包目录和package-info**

```powershell
mkdir -p common/src/main/java/me/cortex/voxy/api
```

创建文件 `common/src/main/java/me/cortex/voxy/api/package-info.java`:
```java
/**
 * 平台抽象接口层。
 * 定义与平台无关的接口，供fabric和neoforge模块实现。
 */
package me.cortex.voxy.api;
```

- [ ] **Step 3: 验证common模块编译**

Run: `gradlew :common:classes`
Expected: 编译成功（可能有警告但无错误）

---

## Chunk 3: Fabric模块配置（保持原有）

### Task 3: 配置fabric模块保持原有功能

**Files:**
- Modify: `fabric/build.gradle` (从原项目移动)
- Modify: `fabric/src/main/resources/fabric.mod.json`

- [ ] **Step 1: 创建fabric/build.gradle**

```groovy
plugins {
    id 'fabric-loom' version "${project.loom_version}"
}

base {
    archivesName = "${project.archives_base_name}-fabric"
}

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
    maven { url = "https://maven.shedaniel.me/" }
    maven { url = "https://maven.terraformersmc.com/releases/" }
}

dependencies {
    // Minecraft
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    mappings loom.officialMojangMappings()
    
    // Fabric Loader
    modImplementation "net.fabricmc:fabric-loader:${project.fabric_loader_version}"
    
    // Fabric API
    modImplementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    
    // Sodium
    modCompileOnly "maven.modrinth:sodium:${project.sodium_version}-fabric"
    modRuntimeOnly "maven.modrinth:sodium:${project.sodium_version}-fabric"
    
    // Iris
    modCompileOnly "maven.modrinth:iris:${project.iris_version}-fabric"
    modRuntimeOnly "maven.modrinth:iris:${project.iris_version}-fabric"
    
    // Lithium
    modImplementation "maven.modrinth:lithium:${project.lithium_version}-fabric"
    
    // ModMenu
    modCompileOnly "maven.modrinth:modmenu:11.0.3"
    modRuntimeOnly "maven.modrinth:modmenu:11.0.3"
    
    // Common模块
    implementation(project(':common'))
    include(implementation(project(':common')))
    
    // 原生库
    include(implementation "org.lwjgl:lwjgl-lmdb:${project.lwjgl_version}")
    include(implementation "org.lwjgl:lwjgl-zstd:${project.lwjgl_version}")
    include(implementation "org.rocksdb:rocksdbjni:${project.rocksdb_version}")
    include(implementation "redis.clients:jedis:${project.jedis_version}")
}

loom {
    accessWidenerPath = file("src/main/resources/voxy.accesswidener")
}

processResources {
    inputs.property "version", project.version
    filesMatching("fabric.mod.json") {
        expand "version": project.version
    }
}
```

- [ ] **Step 2: 移动原有Fabric代码**

将原项目的src目录内容移动到fabric/src:
- `src/main/java/me/cortex/voxy/*` → `fabric/src/main/java/me/cortex/voxy/*`
- `src/main/resources/*` → `fabric/src/main/resources/*`

- [ ] **Step 3: 更新fabric.mod.json**

确保fabric.mod.json中的entrypoints指向正确位置。

- [ ] **Step 4: 验证Fabric模块**

Run: `gradlew :fabric:classes`
Expected: 编译成功

---

## Chunk 4: NeoForge模块配置

### Task 4: 创建neoforge模块Gradle配置

**Files:**
- Create: `neoforge/build.gradle.kts`
- Create: `neoforge/src/main/resources/META-INF/neoforge.mods.toml`
- Create: `neoforge/src/main/resources/META-INF/accesstransformer.cfg`

- [ ] **Step 1: 创建neoforge/build.gradle.kts**

```kotlin
plugins {
    id("net.neoforged.moddev") version("2.0.42-beta")
}

base {
    archivesName = "voxy-neoforge"
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.su5ed.dev/releases") // Forgified Fabric API
}

dependencies {
    // Common模块
    implementation(project(":common"))
    
    // Sodium NeoForge
    implementation("maven.modrinth:sodium:${project.property("sodium_version")}-neoforge")
    
    // Iris NeoForge  
    implementation("maven.modrinth:iris:${project.property("iris_version")}-neoforge")
    
    // Lithium NeoForge (如果存在)
    // implementation("maven.modrinth:lithium:${project.property("lithium_version")}-neoforge")
    
    // Forgified Fabric API模块（Sodium依赖）
    jarJar("org.sinytra.forgified-fabric-api:fabric-api-base:0.4.42+d1308ded19")
    jarJar("org.sinytra.forgified-fabric-api:fabric-renderer-api-v1:3.4.0+9c40919e19")
    jarJar("org.sinytra.forgified-fabric-api:fabric-rendering-data-attachment-v1:0.3.48+73761d2e19")
    jarJar("org.sinytra.forgified-fabric-api:fabric-block-view-api-v2:1.0.10+9afaaf8c19")
    
    // 原生库
    jarJar("org.lwjgl:lwjgl-lmdb:${project.property("lwjgl_version")}")
    jarJar("org.lwjgl:lwjgl-zstd:${project.property("lwjgl_version")}")
    jarJar("org.rocksdb:rocksdbjni:${project.property("rocksdb_version")}")
    jarJar("redis.clients:jedis:${project.property("jedis_version")}")
    jarJar("org.apache.commons:commons-pool2:${project.property("commons_pool2_version")}")
    jarJar("org.lz4:lz4-java:${project.property("lz4_version")}")
}

neoForge {
    version = project.property("neoforge_version") as String
    
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
    
    parchment {
        if (project.hasProperty("parchment_version")) {
            minecraftVersion = project.property("minecraft_version") as String
            mappingsVersion = project.property("parchment_version") as String
        }
    }
    
    runs {
        create("Client") {
            client()
            ideName = "NeoForge/Client"
            
            // JVM参数
            jvmArguments.addAll("-Xmx2G")
        }
        
        create("Server") {
            server()
            ideName = "NeoForge/Server"
        }
    }
    
    mods {
        create("voxy") {
            sourceSet(sourceSets["main"])
            sourceSet(project(":common").sourceSets["main"])
        }
    }
}

tasks {
    jar {
        from(project(":common").sourceSets["main"].output)
        manifest {
            attributes(
                "Specification-Title" to "voxy",
                "Specification-Version" to project.version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }
    
    processResources {
        inputs.property("version", project.version)
        
        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }
}
```

- [ ] **Step 2: 创建neoforge.mods.toml**

```toml
modLoader = "javafml"
loaderVersion = "[4,)"
license = "All-Rights-Reserved"

[[mods]]
modId = "voxy"
version = "${version}"
displayName = "Voxy"
description = '''
远距离LoD渲染模组，使用层次化区块数据提供超远距离渲染。
整合Fakesight区块请求欺骗功能。
'''

[[dependencies.voxy]]
modId = "minecraft"
type = "required"
versionRange = "[1.21.1]"
ordering = "NONE"
side = "CLIENT"

[[dependencies.voxy]]
modId = "neoforge"
type = "required"  
versionRange = "[21.1.82,)"
ordering = "NONE"
side = "CLIENT"

[[dependencies.voxy]]
modId = "sodium"
type = "required"
versionRange = "[0.6.13,)"
ordering = "AFTER"
side = "CLIENT"

[[dependencies.voxy]]
modId = "iris"
type = "optional"
versionRange = "[1.8.8,)"
ordering = "AFTER"
side = "CLIENT"
reason = "提供shader包支持"

[[dependencies.voxy]]
modId = "embeddium"
type = "incompatible"
versionRange = "[0.0.1,)"
ordering = "NONE"
side = "CLIENT"
reason = "Voxy需要原生Sodium，不兼容Embeddium"

[[dependencies.voxy]]
modId = "optifine"
type = "incompatible"
versionRange = "*"
ordering = "NONE"
side = "CLIENT"
reason = "Optifine与Sodium不兼容"

[[mixins]]
config = "voxy-common.mixins.json"

[[mixins]]
config = "voxy-neoforge.mixins.json"
```

- [ ] **Step 3: 创建accesstransformer.cfg**

```properties
# Voxy Access Transformers for NeoForge

# 类访问
public net.minecraft.client.multiplayer.ClientChunkCache$Storage
public com.mojang.blaze3d.platform.GlDebug$LogEntry

# 字段访问
public net.minecraft.client.multiplayer.ClientLevel levelRenderer
public net.minecraft.client.color.block.BlockColors blockColors
public net.minecraft.client.renderer.texture.TextureAtlas mipLevel
public net.minecraft.client.gui.components.BossHealthOverlay events
public net.minecraft.client.multiplayer.MultiPlayerGameMode connection
public-f net.minecraft.world.level.chunk.PalettedContainer$Data palette
public-f net.minecraft.world.level.chunk.PalettedContainer$Data storage
public net.minecraft.client.renderer.RenderType$CompositeRenderType state
public net.minecraft.client.renderer.RenderType$CompositeState textureState
public net.minecraft.world.level.block.StairBlock baseState
public net.minecraft.client.renderer.LightTexture lightTexture
public-f com.mojang.blaze3d.systems.RenderSystem$AutoStorageIndexBuffer name

# 方法访问  
public net.minecraft.client.renderer.RenderStateShard$EmptyTextureStateShard cutoutTexture()
public net.minecraft.client.renderer.texture.MipmapGenerator alphaBlend(IIIIZ)I
public net.minecraft.client.renderer.GameRenderer getFov(Lnet/minecraft/client/Camera;FZ)D
public net.minecraft.client.multiplayer.ClientChunkCache$Storage getChunk(I)Lnet/minecraft/world/level/chunk/LevelChunk;
public net.minecraft.client.multiplayer.ClientChunkCache$Storage getIndex(II)I
```

- [ ] **Step 4: 创建Mixin配置**

创建 `neoforge/src/main/resources/voxy-neoforge.mixins.json`:
```json
{
  "package": "me.cortex.voxy.neoforge.mixin",
  "required": true,
  "compatibilityLevel": "JAVA_21",
  "injectors": {
    "defaultRequire": 1
  },
  "overwrites": {
    "conformVisibility": true
  },
  "client": []
}
```

创建 `common/src/main/resources/voxy-common.mixins.json`:
```json
{
  "package": "me.cortex.voxy.mixin",
  "required": true,
  "compatibilityLevel": "JAVA_21",
  "injectors": {
    "defaultRequire": 1
  },
  "client": []
}
```

- [ ] **Step 5: 验证NeoForge模块配置**

Run: `gradlew :neoforge:configureNeoForge`
Expected: NeoForge配置成功

---

## Chunk 5: 模块间依赖配置

### Task 5: 配置common模块消费接口

**Files:**
- Modify: `neoforge/build.gradle.kts`
- Modify: `fabric/build.gradle`

- [ ] **Step 1: 添加common模块源码配置**

在common/build.gradle中添加:
```groovy
// 添加源码输出配置
configurations {
    create('commonApiJava') { canBeConsumed = true }
    create('commonMainJava') { canBeConsumed = true }
    create('commonMainResources') { canBeConsumed = true }
}

artifacts {
    commonApiJava(sourceSets.main.output.classesDirs)
    commonMainJava(sourceSets.main.output.classesDirs)
    commonMainResources(sourceSets.main.output.resourcesDir)
}
```

- [ ] **Step 2: 验证全项目编译**

Run: `gradlew build`
Expected: 所有模块编译成功（可能有警告）

- [ ] **Step 3: 提交项目结构**

```bash
git add settings.gradle gradle.properties build.gradle
git add common/ fabric/ neoforge/
git commit -m "feat: 初始化多模块项目结构"
```

---

## 测试验证

- [ ] **Step 1: 验证Gradle项目结构**

Run: `gradlew projects`
Expected输出:
```
Root project 'voxy'
+ Project ':common'
+ Project ':fabric'
+ Project ':neoforge'
```

- [ ] **Step 2: 验证依赖解析**

Run: `gradlew :neoforge:dependencies --configuration implementation`
Expected: 显示Sodium、Iris、common等依赖

- [ ] **Step 3: 验证NeoForge运行配置**

Run: `gradlew :neoforge:prepareRuns`
Expected: 运行配置准备成功

---

## 完成标志

- [ ] 项目结构正确：三个模块（common、fabric、neoforge）
- [ ] Gradle配置正确：settings.gradle、gradle.properties、各模块build.gradle
- [ ] NeoForge配置正确：neoforge.mods.toml、accesstransformer.cfg
- [ ] Fabric配置正确：fabric.mod.json、accesswidener
- [ ] 模块间依赖正确：fabric/neoforge依赖common
- [ ] 基础编译成功：`gradlew build`无错误