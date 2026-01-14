plugins {
    kotlin("jvm") version "1.9.24"
    id("fabric-loom") version "1.10.8"
    id("maven-publish") version "0.8.4"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.24"
}

version = project.mod_version
group = project.maven_group

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.terraformersmc.com/")
}

base {
    archivesName.set(project.archives_base_name)
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.minecraft_version}")
    mappings("net.fabricmc.fabric-loader:${project.loader_version}:stable")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.fabric_version}")

    // Kotlin
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    
    // YAML
    implementation("org.yaml:snakeyaml:2.2")
    
    // Commands
    implementation("me.lucko:fabric-permissions-api-v0:0.2-SNAPSHOT")
}

processResources {
    inputs.property("version", project.version)
    filtering("**/fabric.mod.json")

    from(sourceSets["main"].resources.srcDirs) {
        include("**/fabric.mod.json")
        expand(mapOf("version" to project.version))
    }
    from(sourceSets["main"].resources.srcDirs) {
        exclude("**/fabric.mod.json")
    }
}

def targetJavaVersion = 21

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release = targetJavaVersion
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = targetJavaVersion.toString()
    kotlinOptions.languageVersion = "1.9"
    kotlinOptions.freeCompilerArgs += "-Xjvm-default=all"
}

tasks.withType<Jar> {
    // Loom and fabric loader both set archiveClassifier to empty string automatically
    // archiveClassifier = ""
    // Loom is adding some empty directories so we need to clean them up
    doFirst {
        file("src/main/resources/META-INF/jars").deleteRecursively()
    }
}

jar {
    from("LICENSE") {
        rename { "${it}_${base.archivesName.get()}"}
    }
}

// Example configuration to allow publishing using the maven-publish plugin
// This is an alternative to the maven plugin included from the base plugin
subprojects {
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>(mavenArtifactId) {
                from(project.components["java"])
            }
        }

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
            // Add repositories to publish to here.
            // Notice: This block does NOT have the same function as the block in the top level.
            // The repositories here will be used for publishing your artifact, not for
            // retrieving dependencies.
        }
    }
}

loom {
    splits()
}

sourceSets {
    main {
        resources { srcDir("src/generated/resources") }
    }
}