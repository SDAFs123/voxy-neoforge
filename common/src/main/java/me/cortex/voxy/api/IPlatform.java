package me.cortex.voxy.api;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.List;

public interface IPlatform {
    
    boolean isModLoaded(String modId);
    
    Path getConfigDir();
    
    Path getGameDir();
    
    EnvType getEnvironmentType();
    
    String getModVersion();
    
    Optional<Path> getModRootPath();
    
    String getPlatformName();
    
    default List<Consumer<Function<String, Consumer<Boolean>>>> getFrexEntrypoints() {
        return List.of();
    }
}