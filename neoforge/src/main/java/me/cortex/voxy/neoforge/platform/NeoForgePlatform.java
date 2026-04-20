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
        return net.neoforged.fml.loading.FMLLoader.getDist() == net.neoforged.api.distmarker.Dist.CLIENT
            ? EnvType.CLIENT
            : EnvType.SERVER;
    }
    
    @Override
    public String getModVersion() {
        return this.modContainer.getModInfo().getVersion().toString();
    }
    
    @Override
    public Optional<Path> getModRootPath() {
        return Optional.empty();
    }
    
    @Override
    public String getPlatformName() {
        return "neoforge";
    }
}