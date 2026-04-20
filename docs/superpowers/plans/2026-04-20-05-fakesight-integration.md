# Voxy NeoForge移植 - Fakesight功能整合计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development

**Goal:** 将Fakesight的区块请求欺骗功能整合进Voxy配置和Mixin系统

**Architecture:** 扩展VoxyConfig添加requestDistance配置，创建NeoForge兼容的Mixin

**Tech Stack:** Mixin, NeoForge事件系统

---

## Chunk 1: 配置扩展

### Task 1: 扩展VoxyConfig添加Fakesight配置

**Files:**
- Modify: `common/src/main/java/me/cortex/voxy/client/config/VoxyConfig.java`

- [ ] **Step 1: 添加新配置字段**

在VoxyConfig中添加:
```java
// Fakesight区块请求欺骗配置
public boolean enableExtendedRequestDistance = true;
public int requestDistance = 48;  // 向服务器请求的距离（区块）
```

- [ ] **Step 2: 添加getter方法**

```java
public int getRequestDistance() {
    return this.enableExtendedRequestDistance ? this.requestDistance : 
        Minecraft.getInstance().options.renderDistance().get();
}
```

---

## Chunk 2: Mixin实现

### Task 2: 创建IntegratedServerMixin

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/mixin/fakesight/IntegratedServerMixin.java`

- [ ] **Step 1: 创建IntegratedServerMixin**

```java
package me.cortex.voxy.neoforge.mixin.fakesight;

import me.cortex.voxy.client.config.VoxyConfig;
import net.minecraft.client.server.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

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

---

### Task 3: 创建OptionsMixin

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/mixin/fakesight/OptionsMixin.java`

- [ ] **Step 1: 创建OptionsMixin**

```java
package me.cortex.voxy.neoforge.mixin.fakesight;

import me.cortex.voxy.client.config.VoxyConfig;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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

---

## Chunk 3: 配置界面集成

### Task 4: 在配置界面添加Fakesight选项

**Files:**
- Modify: `common/src/main/java/me/cortex/voxy/client/config/VoxyConfigScreenPages.java`

- [ ] **Step 1: 添加扩展区块请求选项组**

在VoxyConfigScreenPages中添加:
```java
// 扩展区块请求设置
public static OptionGroup.Builder createExtendedRequestOptions() {
    return OptionGroup.builder()
        .add(OptionImpl.createBuilder(boolean.class, VoxyConfig.CONFIG)
            .setName(Component.translatable("voxy.config.extendedRequest.enabled"))
            .setTooltip(Component.translatable("voxy.config.extendedRequest.enabled.tooltip"))
            .setControl(opt -> new ToggleControl(opt))
            .setBinding((cfg, val) -> cfg.enableExtendedRequestDistance = val,
                       cfg -> cfg.enableExtendedRequestDistance)
            .setImpact(OptionImpact.HIGH)
            .build())
        .add(OptionImpl.createBuilder(int.class, VoxyConfig.CONFIG)
            .setName(Component.translatable("voxy.config.extendedRequest.distance"))
            .setTooltip(Component.translatable("voxy.config.extendedRequest.distance.tooltip"))
            .setControl(opt -> new SliderControl(opt, 8, 127, 1, 
                v -> Component.literal(Integer.toString(v))))
            .setBinding((cfg, val) -> cfg.requestDistance = val,
                       cfg -> cfg.requestDistance)
            .setImpact(OptionImpact.HIGH)
            .build());
}
```

---

## Chunk 4: 语言文件

### Task 5: 添加语言文件

**Files:**
- Modify: `common/src/main/resources/assets/voxy/lang/en_us.json`

- [ ] **Step 1: 添加英文语言文件**

```json
{
  "voxy.config.extendedRequest.enabled": "扩展区块请求",
  "voxy.config.extendedRequest.enabled.tooltip": "向服务器请求更远的区块数据用于LoD渲染",
  "voxy.config.extendedRequest.distance": "请求距离",
  "voxy.config.extendedRequest.distance.tooltip": "向服务器请求的区块距离（8-127区块）"
}
```

- [ ] **Step 2: 添加中文语言文件**

创建 `common/src/main/resources/assets/voxy/lang/zh_cn.json`:
```json
{
  "voxy.config.extendedRequest.enabled": "扩展区块请求",
  "voxy.config.extendedRequest.enabled.tooltip": "向服务器请求更远的区块数据用于LoD渲染",
  "voxy.config.extendedRequest.distance": "请求距离",
  "voxy.config.extendedRequest.distance.tooltip": "向服务器请求的区块距离（8-127区块）"
}
```

---

## 完成标志

- [ ] VoxyConfig扩展完成
- [ ] IntegratedServerMixin创建完成
- [ ] OptionsMixin创建完成
- [ ] 配置界面集成完成
- [ ] 语言文件添加完成
- [ ] 编译无错误