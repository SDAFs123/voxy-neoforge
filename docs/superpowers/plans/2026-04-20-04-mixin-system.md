# Voxy NeoForge移植 - Mixin系统转换计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development

**Goal:** 将Fabric Mixin系统转换为NeoForge兼容的Mixin配置

**Architecture:** 保持Mixin代码不变，仅修改配置文件格式和加载方式

**Tech Stack:** Mixin, NeoForge Mixin系统, Access Transformer

---

## Chunk 1: Common模块Mixin配置

### Task 1: 创建common模块Mixin配置

**Files:**
- Create: `common/src/main/resources/voxy-common.mixins.json`
- Modify: 各Mixin类确保平台无关

- [ ] **Step 1: 创建voxy-common.mixins.json**

```json
{
  "package": "me.cortex.voxy.mixin",
  "required": true,
  "compatibilityLevel": "JAVA_21",
  "injectors": {
    "defaultRequire": 1
  },
  "overwrites": {
    "conformVisibility": true
  },
  "client": [
    "minecraft.MixinLevelRenderer",
    "minecraft.MixinClientLevel",
    "minecraft.MixinDebugScreenOverlay",
    "minecraft.MixinFogRenderer",
    "minecraft.MixinWindow",
    "minecraft.MixinMinecraft",
    "minecraft.MixinRenderSystem"
  ]
}
```

---

## Chunk 2: NeoForge特定Mixin

### Task 2: 创建NeoForge平台Mixin

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/mixin/minecraft/MixinClientChunkCache.java`
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/mixin/sodium/MixinSodiumWorldRenderer.java`
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/mixin/iris/MixinIris.java`

- [ ] **Step 1: 迁移MixinClientChunkCache**

```java
package me.cortex.voxy.neoforge.mixin.minecraft;

import me.cortex.voxy.api.Platform;
import me.cortex.voxy.client.ICheekyClientChunkCache;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.common.world.service.VoxelIngestService;
import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkCache.class)
public class MixinClientChunkCache implements ICheekyClientChunkCache {
    @Unique
    private static final boolean BOBBY_INSTALLED = Platform.isModLoaded("bobby");

    @Shadow
    private volatile ClientChunkCache.Storage storage;

    @Override
    public @Nullable LevelChunk voxy$cheekyGetChunk(int x, int z) {
        var chunk = this.storage.getChunk(this.storage.getIndex(x, z));
        if (chunk == null) return null;
        if (chunk.getPos().x == x && chunk.getPos().z == z) return chunk;
        return null;
    }

    @Inject(method = "drop", at = @At("HEAD"))
    public void voxy$captureChunkBeforeUnload(ChunkPos pos, CallbackInfo ci) {
        if (VoxyConfig.CONFIG.ingestEnabled && BOBBY_INSTALLED) {
            var chunk = this.voxy$cheekyGetChunk(pos.x, pos.z);
            if (chunk != null) {
                VoxelIngestService.tryAutoIngestChunk(chunk);
            }
        }
    }
}
```

- [ ] **Step 2: 迁移MixinSodiumWorldRenderer**

```java
package me.cortex.voxy.neoforge.mixin.sodium;

import me.cortex.voxy.commonImpl.VoxyCommon;
import net.caffeinemc.mods.sodium.client.gl.device.CommandList;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public class MixinSodiumWorldRenderer {
    @Inject(method = "initRenderer", at = @At("TAIL"), remap = false)
    private void voxy$injectThreadUpdate(CommandList cl, CallbackInfo ci) {
        var vi = VoxyCommon.getInstance();
        if (vi != null) vi.updateDedicatedThreads();
    }
}
```

---

## Chunk 3: Mixin配置文件

### Task 3: 创建完整的NeoForge Mixin配置

**Files:**
- Create: `neoforge/src/main/resources/voxy-neoforge.mixins.json`

- [ ] **Step 1: 创建完整配置**

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
  "client": [
    "minecraft.MixinClientChunkCache",
    "minecraft.MixinLevelRenderer",
    "minecraft.MixinClientLevel",
    "sodium.MixinSodiumWorldRenderer",
    "sodium.MixinRenderSectionManager",
    "iris.MixinIris",
    "iris.MixinIrisRenderingPipeline",
    "fakesight.IntegratedServerMixin",
    "fakesight.OptionsMixin"
  ]
}
```

---

## 完成标志

- [ ] Common Mixin配置创建完成
- [ ] NeoForge特定Mixin迁移完成
- [ ] Mixin配置文件创建完成
- [ ] 编译无错误