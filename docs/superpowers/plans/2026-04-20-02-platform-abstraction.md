# Voxy NeoForge移植 - 平台抽象层计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建平台抽象接口层，将Fabric特定代码抽象为可替换的平台实现

**Architecture:** 定义IPlatform接口和一系列平台服务接口，fabric/neoforge模块分别实现

**Tech Stack:** Java 21, Service Loader模式（可选）

---

## Chunk 1: 平台抽象接口定义

### Task 1: 创建IPlatform核心接口

**Files:**
- Create: `common/src/main/java/me/cortex/voxy/api/IPlatform.java`
- Create: `common/src/main/java/me/cortex/voxy/api/EnvType.java`

- [ ] **Step 1: 创建EnvType枚举**

```java
package me.cortex.voxy.api;

/**
 * 运行环境类型枚举
 */
public enum EnvType {
    CLIENT,
    SERVER
}
```

- [ ] **Step 2: 创建IPlatform接口**

```java
package me.cortex.voxy.api;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 平台抽象接口，提供平台无关的操作方法。
 * Fabric和NeoForge模块需要提供各自的实现。
 */
public interface IPlatform {
    
    /**
     * 检查指定模组是否已加载
     * @param modId 模组ID
     * @return 是否已加载
     */
    boolean isModLoaded(String modId);
    
    /**
     * 获取配置目录路径
     * @return 配置目录
     */
    Path getConfigDir();
    
    /**
     * 获取游戏目录路径
     * @return 游戏目录
     */
    Path getGameDir();
    
    /**
     * 获取当前运行环境类型
     * @return CLIENT或SERVER
     */
    EnvType getEnvironmentType();
    
    /**
     * 获取模组版本字符串
     * @return 版本号
     */
    String getModVersion();
    
    /**
     * 获取模组根路径（用于加载资源）
     * @return 模组根路径
     */
    Optional<Path> getModRootPath();
    
    /**
     * 获取平台名称
     * @return "fabric" 或 "neoforge"
     */
    String getPlatformName();
    
    // 静态实例管理
    private static IPlatform INSTANCE = null;
    
    static IPlatform getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Platform instance not initialized");
        }
        return INSTANCE;
    }
    
    static void setInstance(IPlatform instance) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Platform instance already set");
        }
        INSTANCE = instance;
    }
    
    static boolean hasInstance() {
        return INSTANCE != null;
    }
}
```

- [ ] **Step 3: 创建Platform静态访问类**

```java
package me.cortex.voxy.api;

import java.nio.file.Path;
import java.util.Optional;

/**
 * 平台访问的静态便捷类
 */
public final class Platform {
    
    public static boolean isModLoaded(String modId) {
        return IPlatform.getInstance().isModLoaded(modId);
    }
    
    public static Path getConfigDir() {
        return IPlatform.getInstance().getConfigDir();
    }
    
    public static Path getGameDir() {
        return IPlatform.getInstance().getGameDir();
    }
    
    public static EnvType getEnvironmentType() {
        return IPlatform.getInstance().getEnvironmentType();
    }
    
    public static String getModVersion() {
        return IPlatform.getInstance().getModVersion();
    }
    
    public static Optional<Path> getModRootPath() {
        return IPlatform.getInstance().getModRootPath();
    }
    
    public static String getPlatformName() {
        return IPlatform.getInstance().getPlatformName();
    }
    
    public static boolean isClient() {
        return getEnvironmentType() == EnvType.CLIENT;
    }
    
    public static boolean isServer() {
        return getEnvironmentType() == EnvType.SERVER;
    }
    
    public static boolean isFabric() {
        return "fabric".equals(getPlatformName());
    }
    
    public static boolean isNeoForge() {
        return "neoforge".equals(getPlatformName());
    }
}
```

---

## Chunk 2: Fabric平台实现

### Task 2: 创建FabricPlatform实现

**Files:**
- Create: `fabric/src/main/java/me/cortex/voxy/fabric/platform/FabricPlatform.java`

- [ ] **Step 1: 创建FabricPlatform类**

```java
package me.cortex.voxy.fabric.platform;

import me.cortex.voxy.api.EnvType;
import me.cortex.voxy.api.IPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.Optional;

public class FabricPlatform implements IPlatform {
    
    private final ModContainer modContainer;
    
    public FabricPlatform() {
        this.modContainer = FabricLoader.getInstance()
            .getModContainer("voxy")
            .orElseThrow(() -> new IllegalStateException("Voxy mod container not found"));
    }
    
    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    
    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }
    
    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }
    
    @Override
    public EnvType getEnvironmentType() {
        return FabricLoader.getInstance().getEnvironmentType() == net.fabricmc.api.EnvType.CLIENT
            ? EnvType.CLIENT
            : EnvType.SERVER;
    }
    
    @Override
    public String getModVersion() {
        return this.modContainer.getMetadata().getVersion().getFriendlyString();
    }
    
    @Override
    public Optional<Path> getModRootPath() {
        return this.modContainer.getRootPaths().stream().findFirst();
    }
    
    @Override
    public String getPlatformName() {
        return "fabric";
    }
}
```

- [ ] **Step 2: 在Fabric入口注册平台**

修改 `fabric/src/main/java/me/cortex/voxy/client/VoxyClient.java`:
```java
// 在onInitializeClient方法开头添加
@Override
public void onInitializeClient() {
    // 设置平台实例
    IPlatform.setInstance(new FabricPlatform());
    
    // 原有代码...
    ClientCommandRegistrationCallback.EVENT.register(...);
    ...
}
```

---

## Chunk 3: NeoForge平台实现

### Task 3: 创建NeoForgePlatform实现

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/platform/NeoForgePlatform.java`

- [ ] **Step 1: 创建NeoForgePlatform类**

```java
package me.cortex.voxy.neoforge.platform;

import me.cortex.voxy.api.EnvType;
import me.cortex.voxy.api.IPlatform;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.Optional;

public class NeoForgePlatform implements IPlatform {
    
    private final ModContainer modContainer;
    
    public NeoForgePlatform(ModContainer container) {
        this.modContainer = container;
    }
    
    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    
    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
    
    @Override
    public Path getGameDir() {
        return FMLPaths.GAMEDIR.get();
    }
    
    @Override
    public EnvType getEnvironmentType() {
        // NeoForge环境下，通过物理侧判断
        return net.neoforged.api.distmarker.Dist.CLIENT == 
            net.neoforged.fml.loading.FMLLoader.getDist()
            ? EnvType.CLIENT
            : EnvType.SERVER;
    }
    
    @Override
    public String getModVersion() {
        return this.modContainer.getModInfo().getVersion().toString();
    }
    
    @Override
    public Optional<Path> getModRootPath() {
        // NeoForge中模组路径获取方式不同
        return Optional.empty(); // 或通过其他方式获取
    }
    
    @Override
    public String getPlatformName() {
        return "neoforge";
    }
}
```

---

## Chunk 4: 代码迁移 - 替换FabricLoader调用

### Task 4: 迁移现有代码使用Platform接口

**Files:**
- Modify: `common/src/main/java/me/cortex/voxy/commonImpl/VoxyCommon.java`
- Modify: `common/src/main/java/me/cortex/voxy/client/config/VoxyConfig.java`
- Modify: `common/src/main/java/me/cortex/voxy/common/config/Serialization.java`
- Modify: `common/src/main/java/me/cortex/voxy/common/voxelization/WorldConversionFactory.java`
- Modify: `common/src/main/java/me/cortex/voxy/client/mixin/ClientVoxyMixinPlugin.java`

- [ ] **Step 1: 迁移VoxyCommon.java**

原代码:
```java
import net.fabricmc.loader.api.FabricLoader;
...
ModContainer mod = (ModContainer) FabricLoader.getInstance().getModContainer("voxy").orElse(null);
IS_DEDICATED_SERVER = FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER;
```

新代码:
```java
import me.cortex.voxy.api.Platform;
import me.cortex.voxy.api.IPlatform;
...
// 移除静态初始化块中的FabricLoader调用
// 改为延迟初始化或使用Platform接口
public static final boolean IS_DEDICATED_SERVER = Platform.isServer(); // 需要在平台初始化后调用
public static String MOD_VERSION = null;

public static void initVersion() {
    if (Platform.hasInstance()) {
        MOD_VERSION = Platform.getModVersion();
    } else {
        MOD_VERSION = "<UNKNOWN>";
    }
}
```

- [ ] **Step 2: 迁移VoxyConfig.java**

原代码:
```java
import net.fabricmc.loader.api.FabricLoader;
...
return FabricLoader.getInstance().getConfigDir().resolve("voxy-config.json");
```

新代码:
```java
import me.cortex.voxy.api.Platform;
...
private static Path getConfigPath() {
    return Platform.getConfigDir().resolve("voxy-config.json");
}
```

- [ ] **Step 3: 迁移Serialization.java**

原代码:
```java
import net.fabricmc.loader.api.FabricLoader;
...
var path = FabricLoader.getInstance().getModContainer("voxy").get().getRootPaths().get(0);
```

新代码:
```java
import me.cortex.voxy.api.Platform;
...
var path = Platform.getModRootPath().orElse(Platform.getGameDir());
```

- [ ] **Step 4: 迁移WorldConversionFactory.java**

原代码:
```java
import net.fabricmc.loader.api.FabricLoader;
...
private static final boolean LITHIUM_INSTALLED = FabricLoader.getInstance().isModLoaded("lithium");
```

新代码:
```java
import me.cortex.voxy.api.Platform;
...
private static final boolean LITHIUM_INSTALLED = Platform.isModLoaded("lithium");
```

- [ ] **Step 5: 迁移其他文件中的FabricLoader调用**

搜索并替换所有文件中的FabricLoader调用:
```java
// 搜索模式
FabricLoader.getInstance().isModLoaded("xxx")
// 替换为
Platform.isModLoaded("xxx")
```

涉及的文件:
- `ClientVoxyMixinPlugin.java`
- `MixinClientChunkCache.java`
- `MixinRenderSectionManager.java`
- `IrisUtil.java`
- `ViewportSelector.java`
- `FlashbackCompat.java`

---

## Chunk 5: 测试验证

### Task 5: 编写平台接口测试

**Files:**
- Create: `common/src/test/java/me/cortex/voxy/api/PlatformTest.java`

- [ ] **Step 1: 创建平台接口测试**

```java
package me.cortex.voxy.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlatformTest {
    
    @Test
    void testPlatformNotInitialized() {
        // 重置状态后测试
        assertThrows(IllegalStateException.class, () -> {
            Platform.isModLoaded("test");
        });
    }
    
    @Test
    void testMockPlatform() {
        IPlatform mockPlatform = new MockPlatform();
        IPlatform.setInstance(mockPlatform);
        
        assertTrue(Platform.isModLoaded("voxy"));
        assertEquals(EnvType.CLIENT, Platform.getEnvironmentType());
        assertEquals("mock", Platform.getPlatformName());
    }
    
    // Mock实现用于测试
    private static class MockPlatform implements IPlatform {
        @Override
        public boolean isModLoaded(String modId) {
            return "voxy".equals(modId);
        }
        
        @Override
        public Path getConfigDir() {
            return Path.of("/tmp/config");
        }
        
        @Override
        public Path getGameDir() {
            return Path.of("/tmp/game");
        }
        
        @Override
        public EnvType getEnvironmentType() {
            return EnvType.CLIENT;
        }
        
        @Override
        public String getModVersion() {
            return "1.0.0";
        }
        
        @Override
        public Optional<Path> getModRootPath() {
            return Optional.of(Path.of("/tmp/mod"));
        }
        
        @Override
        public String getPlatformName() {
            return "mock";
        }
    }
}
```

- [ ] **Step 2: 验证编译**

Run: `gradlew :common:test`
Expected: 测试通过

- [ ] **Step 3: 验证Fabric模块**

Run: `gradlew :fabric:classes`
Expected: 编译成功，无FabricLoader相关错误

- [ ] **Step 4: 验证NeoForge模块**

Run: `gradlew :neoforge:classes`
Expected: 编译成功

- [ ] **Step 5: 提交代码**

```bash
git add common/src/main/java/me/cortex/voxy/api/
git add fabric/src/main/java/me/cortex/voxy/fabric/platform/
git add neoforge/src/main/java/me/cortex/voxy/neoforge/platform/
git add common/src/test/
git commit -m "feat: 创建平台抽象接口层"
```

---

## 完成标志

- [ ] IPlatform接口和Platform静态类创建完成
- [ ] FabricPlatform实现完成
- [ ] NeoForgePlatform实现完成  
- [ ] 所有FabricLoader调用已替换为Platform调用
- [ ] 编译无错误
- [ ] 测试通过