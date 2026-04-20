package me.cortex.voxy.neoforge;

import me.cortex.voxy.api.Platform;
import me.cortex.voxy.client.VoxyClient;
import me.cortex.voxy.client.iris.VoxyShaderState;
import me.cortex.voxy.commonImpl.VoxyCommon;
import me.cortex.voxy.neoforge.platform.NeoForgePlatform;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = "voxy", dist = Dist.CLIENT)
public class VoxyNeoForgeMod {
    
    public VoxyNeoForgeMod(IEventBus bus, ModContainer container) {
        Platform.setInstance(new NeoForgePlatform(container));
        VoxyShaderState.markPlatformInitialized();
        
        VoxyCommon.init();
        
        container.registerExtensionPoint(
            IConfigScreenFactory.class,
            (minecraft, parentScreen) -> null
        );
        
        bus.addListener(FMLClientSetupEvent.class, this::onClientSetup);
    }
    
    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            VoxyClient.initVoxyClient();
        });
    }
}