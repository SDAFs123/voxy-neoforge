package me.cortex.voxy.commonImpl;

import me.cortex.voxy.api.EnvType;
import me.cortex.voxy.api.IPlatform;
import me.cortex.voxy.api.Platform;
import me.cortex.voxy.common.Logger;
import me.cortex.voxy.common.config.Serialization;

public class VoxyCommon {
    public static String MOD_VERSION = "<UNKNOWN>";
    public static boolean IS_DEDICATED_SERVER = false;
    public static boolean IS_IN_MINECRAFT = false;
    
    public static void init() {
        if (Platform.hasInstance()) {
            IS_IN_MINECRAFT = true;
            MOD_VERSION = Platform.getModVersion();
            IS_DEDICATED_SERVER = Platform.isServer();
            Serialization.init();
        } else {
            IS_IN_MINECRAFT = false;
            Logger.error("Running voxy without minecraft");
        }
    }
    
    public static boolean isVerificationFlagOn(String name) {
        return isVerificationFlagOn(name, false);
    }
    
    public static boolean isVerificationFlagOn(String name, boolean defaultOn) {
        return System.getProperty("voxy."+name, defaultOn?"true":"false").equals("true");
    }
    
    public static void breakpoint() {
        int breakpoint = 0;
    }
    
    public interface IInstanceFactory {VoxyInstance create();}
    private static VoxyInstance INSTANCE;
    private static IInstanceFactory FACTORY = null;
    
    public static void setInstanceFactory(IInstanceFactory factory) {
        if (FACTORY != null) {
            throw new IllegalStateException("Cannot set instance factory more than once");
        }
        FACTORY = factory;
    }
    
    public static VoxyInstance getInstance() {
        return INSTANCE;
    }
    
    public static void shutdownInstance() {
        if (INSTANCE != null) {
            var instance = INSTANCE;
            INSTANCE = null;
            instance.shutdown();
        }
    }
    
    public static void createInstance() {
        if (FACTORY == null) {
            return;
        }
        if (INSTANCE != null) {
            throw new IllegalStateException("Cannot create multiple instances");
        }
        INSTANCE = FACTORY.create();
    }
    
    public static boolean isAvailable() {
        return FACTORY != null;
    }
    
    public static final boolean IS_MINE_IN_ABYSS = false;
}