package me.cortex.voxy.client;

import me.cortex.voxy.api.Platform;
import me.cortex.voxy.client.core.gl.Capabilities;
import me.cortex.voxy.client.core.rendering.util.SharedIndexBuffer;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.commonImpl.VoxyCommon;
import net.minecraft.client.Minecraft;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.nio.channels.NonWritableChannelException;
import java.util.HashSet;
import java.util.function.Consumer;
import java.util.function.Function;

public class VoxyClient {
    private static final HashSet<String> FREX = new HashSet<>();
    private static FileLock EXCLUSIVE_LOCK;
    private static boolean initialized = false;
    
    public static void initVoxyClient() {
        if (initialized) {
            return;
        }
        initialized = true;
        Capabilities.init();
        
        if (Capabilities.INSTANCE.hasBrokenDepthSampler) {
            Logger.error("AMD broken depth sampler detected, voxy does not work correctly and has been disabled");
        }
        
        boolean systemSupported = Capabilities.INSTANCE.compute && Capabilities.INSTANCE.indirectParameters && !Capabilities.INSTANCE.hasBrokenDepthSampler;
        if (!systemSupported) {
            Logger.error("Voxy is unsupported on your system.");
        }
        
        if (systemSupported && System.getProperty("voxy.exclusiveLock", "false").equalsIgnoreCase("true")) {
            var vf = Minecraft.getInstance().gameDirectory.toPath().resolve(".voxy");
            if (!vf.toFile().isDirectory()) {
                vf.toFile().mkdir();
            }
            try {
                FileOutputStream fis = new FileOutputStream(vf.resolve("voxy.lock").toFile());
                EXCLUSIVE_LOCK = fis.getChannel().lock(0, Long.MAX_VALUE, false);
            } catch (NonWritableChannelException | IOException e) {
                Logger.error("Failed to acquire exclusive voxy lock file, mod will be disabled");
                systemSupported = false;
            }
        }
        
        if (systemSupported) {
            SharedIndexBuffer.INSTANCE.id();
            VoxyCommon.setInstanceFactory(VoxyClientInstance::new);
            
            if (!Capabilities.INSTANCE.subgroup) {
                Logger.warn("GPU does not support subgroup operations, expect some performance degradation");
            }
        }
        
        initFrex();
    }
    
    private static void initFrex() {
        if (!Platform.hasInstance()) return;
        if (!Platform.isFabric()) return;
        
        try {
            for (var entrypoint : Platform.getFrexEntrypoints()) {
                entrypoint.accept(name -> active -> {
                    if (active) {
                        FREX.add(name);
                    } else {
                        FREX.remove(name);
                    }
                });
            }
        } catch (Exception e) {
            Logger.error("Failed to init FREX", e);
        }
    }
    
    public static boolean isFrexActive() {
        return !FREX.isEmpty();
    }
    
    public static int getOcclusionDebugState() {
        return 0;
    }
    
    public static boolean disableSodiumChunkRender() {
        return false;
    }
}