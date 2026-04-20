package me.cortex.voxy.fabric;

import me.cortex.voxy.api.Platform;
import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.mixin.iris.MixinStandardMacros;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.fabric.platform.FabricPlatform;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class VoxyFabricMod implements ModInitializer, ClientModInitializer {
    
    @Override
    public void onInitialize() {
        Platform.setInstance(new FabricPlatform());
        MixinStandardMacros.markPlatformInitialized();
        VoxyCommon.init();
    }
    
    @Override
    public void onInitializeClient() {
        VoxyClient.initVoxyClient();
    }
}