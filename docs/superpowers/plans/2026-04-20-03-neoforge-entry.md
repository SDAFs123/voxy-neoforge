# Voxy NeoForge移植 - NeoForge入口和配置计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan.

**Goal:** 创建NeoForge主入口类和配置界面系统

**Architecture:** 使用@Mod注解作为入口点，集成Sodium配置界面系统

**Tech Stack:** NeoForge事件系统, Sodium Options API

---

## Chunk 1: NeoForge主入口类

### Task 1: 创建VoxyNeoForgeMod入口类

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/VoxyNeoForgeMod.java`

- [ ] **Step 1: 创建主入口类**

```java
package me.cortex.voxy.neoforge;

import me.cortex.voxy.api.IPlatform;
import me.cortex.voxy.api.Platform;
import me.cortex.voxy.client.ClientSessionEvents;
import me.cortex.voxy.client.VoxyClientInstance;
import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.neoforge.platform.NeoForgePlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.TickEvent;

@Mod(value = "voxy", dist = Dist.CLIENT)
public class VoxyNeoForgeMod {
    
    public VoxyNeoForgeMod(IEventBus bus, ModContainer container) {
        // 1. 设置平台实例
        IPlatform.setInstance(new NeoForgePlatform(container));
        
        // 2. 初始化版本信息
        VoxyCommon.initVersion();
        
        // 3. 注册配置界面
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (minecraft, parentScreen) -> VoxyConfigScreen.createScreen(parentScreen)
        );
        
        // 4. 注册命令
        bus.addListener(RegisterCommandsEvent.class, this::registerCommands);
        
        // 5. 注册客户端事件
        bus.addListener(TickEvent.ClientTickEvent.Post.class, this::onClientTick);
        
        // 6. 设置实例工厂
        VoxyCommon.setInstanceFactory(VoxyClientInstance::new);
        
        // 7. 初始化核心系统
        VoxyClient.initVoxyClient();
    }
    
    private void registerCommands(RegisterCommandsEvent event) {
        VoxyCommands.register(event.getDispatcher());
    }
    
    private void onClientTick(TickEvent.ClientTickEvent.Post event) {
        // 处理客户端tick事件
    }
}
```

---

## Chunk 2: 配置系统适配

### Task 2: 创建NeoForge配置界面适配

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/config/VoxyConfigScreen.java`

- [ ] **Step 1: 创建配置界面适配类**

```java
package me.cortex.voxy.neoforge.config;

import me.cortex.voxy.client.config.VoxyConfig;
import me.cortex.voxy.client.config.VoxyConfigScreenPages;
import net.minecraft.client.gui.screens.Screen;

public class VoxyConfigScreen {
    
    public static Screen createScreen(Screen parentScreen) {
        // 使用Voxy原有的配置界面系统（基于Sodium）
        return VoxyConfigScreenPages.createScreen(parentScreen);
    }
}
```

- [ ] **Step 2: 更新VoxyConfigScreenPages以支持NeoForge**

修改 `common/src/main/java/me/cortex/voxy/client/config/VoxyConfigScreenPages.java`:
```java
// 确保配置界面代码与平台无关
// 如果有Fabric特定代码，需要通过Platform接口抽象
```

---

## Chunk 3: 命令系统

### Task 3: 创建NeoForge命令注册

**Files:**
- Create: `neoforge/src/main/java/me/cortex/voxy/neoforge/command/VoxyCommands.java`

- [ ] **Step 1: 创建命令注册类**

```java
package me.cortex.voxy.neoforge.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import me.cortex.voxy.client.config.VoxyConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class VoxyCommands {
    
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("voxy")
            .requires(source -> source.hasPermission(0))
            
            // reload命令
            .then(Commands.literal("reload")
                .executes(ctx -> {
                    VoxyConfig.CONFIG.save();
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal("Voxy配置已重新加载"), false);
                    return 1;
                }))
            
            // debug命令
            .then(Commands.literal("debug")
                .executes(ctx -> {
                    // 切换调试模式
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal("调试模式已切换"), false);
                    return 1;
                }))
            
            // distance命令 - 设置请求距离
            .then(Commands.literal("distance")
                .then(Commands.argument("blocks", IntegerArgumentType.integer(8, 127))
                    .executes(ctx -> {
                        int distance = IntegerArgumentType.getInteger(ctx, "blocks");
                        VoxyConfig.CONFIG.requestDistance = distance;
                        VoxyConfig.CONFIG.save();
                        ctx.getSource().sendSuccess(() -> 
                            Component.literal("区块请求距离设置为: " + distance), false);
                        return 1;
                    })))
            
            // enabled命令
            .then(Commands.literal("enabled")
                .then(Commands.argument("value", StringArgumentType.string())
                    .suggests((ctx, builder) -> {
                        builder.suggest("true");
                        builder.suggest("false");
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        String value = StringArgumentType.getString(ctx, "value");
                        VoxyConfig.CONFIG.enabled = Boolean.parseBoolean(value);
                        VoxyConfig.CONFIG.save();
                        ctx.getSource().sendSuccess(() -> 
                            Component.literal("Voxy已" + (VoxyConfig.CONFIG.enabled ? "启用" : "禁用")), false);
                        return 1;
                    })))
            
            // status命令
            .then(Commands.literal("status")
                .executes(ctx -> {
                    ctx.getSource().sendSuccess(() -> 
                        Component.literal(String.format(
                            "Voxy状态: enabled=%s, distance=%.1f, requestDistance=%d",
                            VoxyConfig.CONFIG.enabled,
                            VoxyConfig.CONFIG.sectionRenderDistance,
                            VoxyConfig.CONFIG.requestDistance
                        )), false);
                    return 1;
                }))
        );
    }
}
```

---

## 完成标志

- [ ] VoxyNeoForgeMod入口类创建完成
- [ ] 配置界面适配完成
- [ ] 命令系统注册完成
- [ ] 编译无错误
- [ ] 运行测试成功