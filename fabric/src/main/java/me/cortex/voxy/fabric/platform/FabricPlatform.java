package me.cortex.voxy.fabric.platform;

import me.cortex.voxy.api.EnvType;
import me.cortex.voxy.api.IPlatform;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

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
    
    @Override
    @SuppressWarnings("unchecked")
    public List<Consumer<Function<String, Consumer<Boolean>>>> getFrexEntrypoints() {
        try {
            return (List) FabricLoader.getInstance().getEntrypoints("frex_flawless_frames", Consumer.class);
        } catch (Exception e) {
            return List.of();
        }
    }
}