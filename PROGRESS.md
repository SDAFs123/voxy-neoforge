---
## Goal

将Voxy LoD渲染模组从Fabric完全重写到NeoForge 1.21.1，同时整合Fakesight区块请求欺骗功能。要求：
- 采用多模块架构（common/fabric/neoforge）
- 保持Sodium和Iris依赖
- 保持原有包名me.cortex.voxy
- 保持Mod ID为voxy
- 继承原版本号0.2.14-alpha
- 保留全部存储后端（LMDB/RocksDB/Redis）
- 使用Sodium配置系统
- 整合Fakesight功能
- 更轻量化，更好的渲染效果

## Instructions

- 不允许简化，只允许修复
- 参考Sodium的多模块架构实现
- 所有原生库需要通过jarJar嵌入到NeoForge模块
- Fabric版本需要保持原有功能
- NeoForge版本需要完全用NeoForge方法重写
- 使用代理10808进行下载构建

## Discoveries

1. **Voxy项目结构**: 约100+ Java文件，分为client、common、commonImpl三大包
2. **Sodium NeoForge实现**: 使用ModDevGradle构建
3. **NeoForge版本兼容**: Sodium需要NeoForge 21.1.115+，已升级到21.1.122
4. **Mixin配置命名**: NeoForge需要使用`common.voxy.mixins.json`
5. **VoxyClient多次初始化**: 需要添加`initialized`标志防止重复调用
6. **ModDevGradle运行时依赖**: 使用`additionalRuntimeClasspath`配置外部库
7. **Access Transformer**: 需要添加`PalettedContainer.data`字段访问权限
8. **LWJGL natives**: 需要添加natives-windows依赖
9. **Fakesight实现**: 通过IntegratedServerMixin修改服务器视野距离
10. **渲染距离单位**: sectionRenderDistance应为区块距离（int类型）
11. **Fabric API必要性**: **Voxy不需要jarJar Forgified Fabric API**，Sodium/Iris自带

## Accomplished

已完成：
- ✅ 多模块项目结构搭建
- ✅ 平台抽象接口及实现
- ✅ VoxyFabricMod/VoxyNeoForgeMod入口类
- ✅ VoxyClient.initVoxyClient()多次调用防护
- ✅ NeoForge版本升级到21.1.122
- ✅ Mixin配置命名修复
- ✅ Gradle构建成功
- ✅ Mixin正确加载
- ✅ 依赖运行时加载问题解决
- ✅ Access Transformer配置
- ✅ LWJGL natives依赖配置
- ✅ NeoForge运行测试成功
- ✅ Fakesight功能整合到Sodium配置界面
- ✅ 渲染距离配置修复（改为int类型，直接显示区块数16-512）
- ✅ 移除不必要的Fabric API jarJar依赖
- ✅ 创建README声明文档

## 配置界面改进

### 渲染距离滑块
- 直接显示区块距离：`128 chunks`（之前显示奇怪的数值）
- 范围：16-512区块
- 类型：int（之前float导致混淆）

### Fakesight选项
- 启用扩展区块请求（布尔）
- 请求距离：8-127区块（整数滑块）

## Relevant files / directories

**核心配置文件：**
- `settings.gradle` - 多模块配置
- `gradle.properties` - 版本配置
- `neoforge/build.gradle.kts` - NeoForge构建（已移除Fabric API）
- `common/build.gradle` - common模块

**Fakesight相关文件：**
- `common/src/main/java/me/cortex/voxy/client/config/VoxyConfig.java` - 配置（sectionRenderDistance改为int）
- `common/src/main/java/me/cortex/voxy/client/config/VoxyConfigScreenPages.java` - 配置界面
- `neoforge/src/main/java/me/cortex/voxy/neoforge/mixin/fakesight/` - Mixin实现

**渲染相关修复：**
- `HierarchicalOcclusionTraverser.java` - 距离公式修复
- `VoxyUniforms.java` - uniform值修复
- `VoxyRenderSystem.java` - setRenderDistance类型修复

## 依赖分析

### Voxy需要jarJar的依赖
- ✅ lwjgl-lmdb, lwjgl-zstd
- ✅ rocksdbjni, jedis, commons-pool2, lz4-java
- ✅ mixinextras-common

### 不需要jarJar的依赖
- ❌ fabric-api-base（Sodium自带）
- ❌ fabric-renderer-api-v1（Sodium自带）
- ❌ fabric-rendering-data-attachment-v1（Sodium自带）
- ❌ fabric-block-view-api-v2（Sodium自带）

## 关键技术点

### 渲染距离公式
```java
// HierarchicalOcclusionTraverser.java
// 方块距离平方用于圆形渲染范围
Math.pow(sectionRenderDistance * 16.0, 2)
```

### VoxyUniforms
```java
// 区块距离
uniform1i("vxRenderDistance", sectionRenderDistance)
// 方块距离
uniform1i("dhRenderDistance", sectionRenderDistance * 16)
```

### Fakesight Mixin
```java
@ModifyArg(method = "tickServer", ...)
private int voxy$modifyRenderDistance(int original) {
    return VoxyConfig.CONFIG.requestDistance;
}
```