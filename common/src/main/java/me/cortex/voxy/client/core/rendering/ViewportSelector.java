package me.cortex.voxy.client.core.rendering;

import me.cortex.voxy.client.core.util.IrisUtil;
import me.cortex.voxy.api.Platform;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ViewportSelector <T extends Viewport<?>> {
    public static final boolean VIVECRAFT_INSTALLED = Platform.isModLoaded("vivecraft");

    private final Supplier<T> creator;
    private final T defaultViewport;
    private final Map<Object, T> extraViewports = new HashMap<>();

    public ViewportSelector(Supplier<T> viewportCreator) {
        this.creator = viewportCreator;
        this.defaultViewport = viewportCreator.get();
    }

    private T getOrCreate(Object holder) {
        return this.extraViewports.computeIfAbsent(holder, a->this.creator.get());
    }

    private T getVivecraftViewport() {
        if (!VIVECRAFT_INSTALLED) {
            return null;
        }
        try {
            var vrApiClass = Class.forName("org.vivecraft.api.client.VRRenderingAPI");
            var instanceMethod = vrApiClass.getMethod("instance");
            var vrApi = instanceMethod.invoke(null);
            var getCurrentRenderPassMethod = vrApiClass.getMethod("getCurrentRenderPass");
            var pass = getCurrentRenderPassMethod.invoke(vrApi);
            if (pass == null) {
                return null;
            }
            var vanillaField = Class.forName("org.vivecraft.api.client.data.RenderPass").getField("VANILLA");
            var vanilla = vanillaField.get(null);
            if (pass == vanilla) {
                return null;
            }
            return this.getOrCreate(pass);
        } catch (Exception e) {
            return null;
        }
    }

    private static final Object IRIS_SHADOW_OBJECT = new Object();
    public T getViewport() {
        T viewport = null;
        if (viewport == null && VIVECRAFT_INSTALLED) {
            viewport = getVivecraftViewport();
        }

        if (viewport == null && IrisUtil.irisShadowActive()) {
            viewport = this.getOrCreate(IRIS_SHADOW_OBJECT);
        }

        if (viewport == null) {
            viewport = this.defaultViewport;
        }
        return viewport;
    }

    public void free() {
        this.defaultViewport.delete();
        this.extraViewports.values().forEach(Viewport::delete);
        this.extraViewports.clear();
    }
}
