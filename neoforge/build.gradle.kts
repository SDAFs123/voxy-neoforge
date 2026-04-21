buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.ow2.asm:asm:9.7")
        classpath("org.ow2.asm:asm-commons:9.7")
    }
}

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

configurations {
    create("lz4Flatten")
    create("rocksdbRaw")
}

dependencies {
    implementation(project(":common"))
    
    implementation("maven.modrinth:sodium:${project.property("sodium_version")}-neoforge")
    implementation("maven.modrinth:iris:${project.property("iris_version")}-neoforge")
    
    implementation("org.rocksdb:rocksdbjni:${project.property("rocksdb_version")}")
    implementation("redis.clients:jedis:${project.property("jedis_version")}")
    implementation("org.apache.commons:commons-pool2:${project.property("commons_pool2_version")}")
    implementation("org.lz4:lz4-java:${project.property("lz4_version")}")
    
    "lz4Flatten"("org.lz4:lz4-java:${project.property("lz4_version")}")
    "rocksdbRaw"("org.rocksdb:rocksdbjni:${project.property("rocksdb_version")}")
    
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
    
    jarJar("org.lwjgl:lwjgl-lmdb:${project.property("lwjgl_version")}")
    jarJar("org.lwjgl:lwjgl-zstd:${project.property("lwjgl_version")}")
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
    val relocateLz4Classes by registering(Sync::class) {
        from(zipTree(configurations["lz4Flatten"].singleFile))
        into(layout.buildDirectory.dir("relocated-lz4"))
        eachFile {
            val path = this.path
            if (path.startsWith("org/lz4/")) {
                this.path = path.replace("org/lz4/", "me/cortex/voxy/lib/lz4/")
            } else if (path.startsWith("net/jpountz/")) {
                this.path = path.replace("net/jpountz/", "me/cortex/voxy/lib/lz4/jpountz/")
            }
        }
        exclude("META-INF/**")
        exclude("module-info.class")
        exclude("**/*.dylib")
        exclude("**/*.so")
    }
    
    val relocateLz4Refs by registering {
        dependsOn("compileJava", project(":common").tasks["compileJava"])
        
        doLast {
            val remapper = object : org.objectweb.asm.commons.Remapper() {
                override fun map(typeName: String): String {
                    return when {
                        typeName.startsWith("org/lz4/") -> typeName.replace("org/lz4/", "me/cortex/voxy/lib/lz4/")
                        typeName.startsWith("net/jpountz/") -> typeName.replace("net/jpountz/", "me/cortex/voxy/lib/lz4/jpountz/")
                        else -> typeName
                    }
                }
            }
            
            val classDirs = listOf(
                project(":common").sourceSets["main"].output.classesDirs.files,
                sourceSets["main"].output.classesDirs.files
            ).flatten()
            
            classDirs.forEach { dir ->
                dir.walkTopDown()
                    .filter { it.isFile && it.extension == "class" }
                    .forEach { classFile ->
                        val bytes = classFile.readBytes()
                        val reader = org.objectweb.asm.ClassReader(bytes)
                        val writer = org.objectweb.asm.ClassWriter(reader, 0)
                        val visitor = org.objectweb.asm.commons.ClassRemapper(writer, remapper)
                        reader.accept(visitor, 0)
                        val newBytes = writer.toByteArray()
                        if (!bytes.contentEquals(newBytes)) {
                            classFile.writeBytes(newBytes)
                            println("Relocated lz4 refs in: ${classFile.relativeTo(dir)}")
                        }
                    }
            }
        }
    }
    
    jar {
        dependsOn(relocateLz4Classes, relocateLz4Refs)
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        from(project(":common").sourceSets["main"].output) {
            exclude("**/*.json")
        }
        from(layout.buildDirectory.dir("relocated-lz4"))
        manifest {
            attributes(
                "Specification-Title" to "voxy",
                "Specification-Version" to project.version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
    }
    
    val stripRocksdbNatives by registering(Jar::class) {
        from(zipTree(configurations["rocksdbRaw"].singleFile))
        archiveClassifier = "rocksdb-stripped"
        exclude("**/*.so")
        exclude("**/*.dylib")
        exclude("**/*.jnilib")
        exclude("**/*win32*")
        exclude("META-INF/MANIFEST.MF")
    }
    
    val stripNatives by registering(Copy::class) {
        dependsOn(jar, stripRocksdbNatives)
        from(zipTree(jar.flatMap { it.archiveFile }))
        into(layout.buildDirectory.dir("stripped-jar"))
        exclude("**/*.so")
        exclude("**/*.dylib")
        exclude("**/*win32*")
        exclude("META-INF/jarjar/rocksdbjni*.jar")
    }
    
    val finalJar by registering(Jar::class) {
        dependsOn(stripNatives, stripRocksdbNatives)
        archiveClassifier = ""
        from(layout.buildDirectory.dir("stripped-jar"))
        from(zipTree(stripRocksdbNatives.flatMap { it.archiveFile })) {
            into("META-INF/jarjar")
        }
        manifest {
            attributes(
                "Specification-Title" to "voxy",
                "Specification-Version" to project.version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        }
        doLast {
            val size = archiveFile.get().asFile.length() / 1024 / 1024
            println("Final jar size: ${size}MB (stripped non-Windows natives)")
        }
    }
    
    assemble {
        dependsOn(finalJar)
    }
    
    processResources {
        inputs.property("version", project.version)
        filesMatching("META-INF/neoforge.mods.toml") {
            expand("version" to project.version)
        }
    }
}