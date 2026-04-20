package me.cortex.voxy.client.iris;

public class VoxyShaderState {
    private static boolean platformInitialized = false;
    
    public static void markPlatformInitialized() {
        platformInitialized = true;
    }
    
    public static boolean isPlatformInitialized() {
        return platformInitialized;
    }
}