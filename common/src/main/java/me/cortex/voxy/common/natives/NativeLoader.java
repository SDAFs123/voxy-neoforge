package me.cortex.voxy.common.natives;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class NativeLoader {
    private static final String TEMP_DIR_NAME = "voxy_natives_" + System.currentTimeMillis();
    private static Path tempDir;
    private static boolean rocksdbLoaded = false;
    
    public static synchronized void loadRocksDB() {
        if (rocksdbLoaded) return;
        
        String os = detectOS();
        String arch = detectArch();
        String nativeName = getRocksDBNativeName(os, arch);
        
        if (nativeName == null) {
            System.err.println("[Voxy] Unsupported platform for RocksDB: " + os + "/" + arch);
            System.err.println("[Voxy] RocksDB storage backend will be unavailable");
            return;
        }
        
        try {
            tempDir = createTempDir();
            Path nativePath = extractRocksDBNative(nativeName, tempDir);
            
            if (nativePath != null && nativePath.toFile().exists()) {
                System.load(nativePath.toFile().getAbsolutePath());
                rocksdbLoaded = true;
                System.out.println("[Voxy] Loaded RocksDB native: " + nativeName + " for " + os + "/" + arch);
            } else {
                System.err.println("[Voxy] Could not find RocksDB native: " + nativeName);
            }
        } catch (Exception e) {
            System.err.println("[Voxy] Failed to load RocksDB native: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static Path extractRocksDBNative(String nativeName, Path targetDir) throws IOException {
        Path targetPath = targetDir.resolve(nativeName);
        
        if (targetPath.toFile().exists()) {
            return targetPath;
        }
        
        Enumeration<URL> resources = NativeLoader.class.getClassLoader().getResources("META-INF/jarjar/rocksdbjni-" + getVersion() + ".jar");
        while (resources.hasMoreElements()) {
            URL jarUrl = resources.nextElement();
            try (ZipInputStream zis = new ZipInputStream(jarUrl.openStream())) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals(nativeName) || 
                        entry.getName().endsWith("/" + nativeName)) {
                        Files.copy(zis, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        return targetPath;
                    }
                }
            }
        }
        
        InputStream direct = NativeLoader.class.getClassLoader().getResourceAsStream(nativeName);
        if (direct != null) {
            Files.copy(direct, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath;
        }
        
        direct = NativeLoader.class.getClassLoader().getResourceAsStream("META-INF/natives/" + nativeName);
        if (direct != null) {
            Files.copy(direct, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath;
        }
        
        return null;
    }
    
    private static String getVersion() {
        return "10.2.1";
    }
    
    private static Path createTempDir() throws IOException {
        Path base = Paths.get(System.getProperty("java.io.tmpdir"), TEMP_DIR_NAME);
        Files.createDirectories(base);
        return base;
    }
    
    private static String detectOS() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (os.contains("win")) return "windows";
        if (os.contains("mac") || os.contains("darwin")) return "macos";
        if (os.contains("linux") || os.contains("nix") || os.contains("nux") || os.contains("aix")) return "linux";
        if (os.contains("sunos") || os.contains("solaris")) return "solaris";
        if (os.contains("freebsd")) return "freebsd";
        return os.replace(" ", "_");
    }
    
    private static String detectArch() {
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
        if (arch.matches("amd64|x86_64|x64")) return "x64";
        if (arch.matches("x86|i386|i486|i586|i686")) return "x86";
        if (arch.matches("aarch64|arm64")) return "arm64";
        if (arch.matches("arm")) return "arm";
        if (arch.matches("ppc64|powerpc64")) return "ppc64";
        if (arch.matches("ppc64le|powerpc64le")) return "ppc64le";
        if (arch.matches("s390x")) return "s390x";
        if (arch.matches("riscv64")) return "riscv64";
        return arch.replace(" ", "_");
    }
    
    private static String getRocksDBNativeName(String os, String arch) {
        if (os.equals("windows")) {
            if (arch.equals("x64")) return "librocksdbjni-win64.dll";
            if (arch.equals("x86")) return "librocksdbjni-win32.dll";
        } else if (os.equals("linux")) {
            if (arch.equals("x64")) return "librocksdbjni-linux64.so";
            if (arch.equals("arm64")) return "librocksdbjni-linux-aarch64.so";
            if (arch.equals("ppc64le")) return "librocksdbjni-linux-ppc64le.so";
            if (arch.equals("s390x")) return "librocksdbjni-linux-s390x.so";
            if (arch.equals("riscv64")) return "librocksdbjni-linux-riscv64.so";
        } else if (os.equals("macos")) {
            if (arch.equals("x64")) return "librocksdbjni-osx-x86_64.jnilib";
            if (arch.equals("arm64")) return "librocksdbjni-osx-arm64.jnilib";
        }
        return null;
    }
    
    public static boolean isRocksDBAvailable() {
        return rocksdbLoaded;
    }
    
    public static void cleanup() {
        if (tempDir != null && tempDir.toFile().exists()) {
            try {
                Files.walk(tempDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
            } catch (IOException ignored) {}
        }
    }
}