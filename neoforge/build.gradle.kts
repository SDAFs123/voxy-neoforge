plugins {
    id("net.neoforged.moddev") version("2.0.42-beta")
}

base {
    archivesName = "voxy-neoforge"
}

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://api.modrinth.com/maven")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.su5ed.dev/releases")
}

dependencies {
    implementation(project(":common"))
    
    implementation("maven.modrinth:sodium:${project.property("sodium_version")}-neoforge")
    implementation("maven.modrinth:iris:${project.property("iris_version")}-neoforge")
    
    implementation("org.rocksdb:rocksdbjni:${project.property("rocksdb_version")}")
    implementation("redis.clients:jedis:${project.property("jedis_version")}")
    implementation("org.apache.commons:commons-pool2:${project.property("commons_pool2_version")}")
    implementation("org.lz4:lz4-java:${project.property("lz4_version")}")
    
    additionalRuntimeClasspath("org.rocksdb:rocksdbjni:${project.property("rocksdb_version")}")
    additionalRuntimeClasspath("redis.clients:jedis:${project.property("jedis_version")}")
    additionalRuntimeClasspath("org.apache.commons:commons-pool2:${project.property("commons_pool2_version")}")
    additionalRuntimeClasspath("org.lz4:lz4-java:${project.property("lz4_version")}")
    additionalRuntimeClasspath("org.lwjgl:lwjgl:${project.property("lwjgl_version")}:natives-windows")
    additionalRuntimeClasspath("org.lwjgl:lwjgl-lmdb:${project.property("lwjgl_version")}")
    additionalRuntimeClasspath("org.lwjgl:lwjgl-lmdb:${project.property("lwjgl_version")}:natives-windows")
    additionalRuntimeClasspath("org.lwjgl:lwjgl-zstd:${project.property("lwjgl_version")}")
    additionalRuntimeClasspath("org.lwjgl:lwjgl-zstd:${project.property("lwjgl_version")}:natives-windows")
    additionalRuntimeClasspath("org.tukaani:xz:1.10")
    additionalRuntimeClasspath("it.unimi.dsi:fastutil:8.5.12")
    additionalRuntimeClasspath("org.joml:joml:1.10.5")
    
    jarJar("io.github.llamalad7:mixinextras-common:0.4.1")
    jarJar("org.lwjgl:lwjgl-lmdb:${project.property("lwjgl_version")}")
    jarJar("org.lwjgl:lwjgl-zstd:${project.property("lwjgl_version")}")
    jarJar("org.rocksdb:rocksdbjni:${project.property("rocksdb_version")}")
    jarJar("redis.clients:jedis:${project.property("jedis_version")}")
    jarJar("org.apache.commons:commons-pool2:${project.property("commons_pool2_version")}")
}

neoForge {
    version = project.property("neoforge_version") as String
    
    accessTransformers {
        file("src/main/resources/META-INF/accesstransformer.cfg")
    }
    
    runs {
        create("Client") {
            client()
            ideName = "NeoForge/Client"
            jvmArguments.addAll("-Xmx4G", "-XX:+UseG1GC")
        }
        create("Server") {
            server()
            ideName = "NeoForge/Server"
        }
    }
    
    mods {
        create("voxy") {
            sourceSet(sourceSets["main"])
            sourceSet(project(":common").sourceSets["main"])
        }
    }
}

tasks {
    jar {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(project(":common").sourceSets["main"].output) {
            exclude("**/*.json")
        }
        manifest {
            attributes(
                "Specification-Title" to "voxy",
                "Specification-Version" to project.version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }
    
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }
}