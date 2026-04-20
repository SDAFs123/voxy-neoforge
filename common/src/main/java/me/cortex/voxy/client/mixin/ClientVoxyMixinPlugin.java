package me.cortex.voxy.client.mixin;

import me.cortex.voxy.api.Platform;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ClientVoxyMixinPlugin implements IMixinConfigPlugin {
    private static boolean valkyrienSkiesInstalled;
    private static boolean nvidiumInstalled;
    private static boolean connectorInstalled = false;
    private static boolean initialized = false;

    private static void initialize() {
        if (initialized) return;
        initialized = true;
        try {
            if (Platform.hasInstance()) {
                valkyrienSkiesInstalled = Platform.isModLoaded("valkyrienskies");
                nvidiumInstalled = Platform.isModLoaded("nvidium");
                connectorInstalled = Platform.isModLoaded("connector");
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) { return true; }

    @Override public List<String> getMixins() {
        initialize();
        List<String> mixins = new ArrayList<>();
        if (valkyrienSkiesInstalled && !nvidiumInstalled) {
            mixins.add("sodium.MixinSodiumWorldRendererVS");
        } else {
            mixins.add("sodium.MixinDefaultChunkRenderer");
        }

        if (connectorInstalled) {
            mixins.add("sodium.MixinShaderLoader");
        }

        return mixins;
    }

    @Override
    public String getRefMapperConfig() { return null; }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}