package me.cortex.voxy.client.mixin.iris;

import me.cortex.voxy.client.core.util.IrisUtil;
import net.irisshaders.iris.mixin.LevelRendererAccessor;
import net.irisshaders.iris.shadows.ShadowRenderer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShadowRenderer.class)
public class MixinShadowRenderer {
    @Inject(method = "renderShadows", at = @At("HEAD"))
    private static void voxy$onShadowRenderStart(LevelRendererAccessor levelRendererAccessor, Camera camera, CallbackInfo ci) {
        IrisUtil.setShadowActive(true);
    }
    
    @Inject(method = "renderShadows", at = @At("TAIL"))
    private static void voxy$onShadowRenderEnd(LevelRendererAccessor levelRendererAccessor, Camera camera, CallbackInfo ci) {
        IrisUtil.setShadowActive(false);
    }
}