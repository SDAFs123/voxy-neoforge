package me.cortex.voxy.api;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;

public final class Platform {
    
    private static IPlatform INSTANCE = null;
    
    public static IPlatform getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException("Platform instance not initialized");
        }
        return INSTANCE;
    }
    
    public static void setInstance(IPlatform instance) {
        if (INSTANCE != null) {
            throw new IllegalStateException("Platform instance already set");
        }
        INSTANCE = instance;
    }
    
    public static boolean hasInstance() {
        return INSTANCE != null;
    }
    
    public static void resetInstance() {
        INSTANCE = null;
    }
    
    public static boolean isModLoaded(String modId) {
        return getInstance().isModLoaded(modId);
    }
    
    public static Path getConfigDir() {
        return getInstance().getConfigDir();
    }
    
    public static Path getGameDir() {
        return getInstance().getGameDir();
    }
    
    public static EnvType getEnvironmentType() {
        return getInstance().getEnvironmentType();
    }
    
    public static String getModVersion() {
        return getInstance().getModVersion();
    }
    
    public static Optional<Path> getModRootPath() {
        return getInstance().getModRootPath();
    }
    
    public static String getPlatformName() {
        return getInstance().getPlatformName();
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
    
    public static List<Consumer<Function<String, Consumer<Boolean>>>> getFrexEntrypoints() {
        return getInstance().getFrexEntrypoints();
    }
}