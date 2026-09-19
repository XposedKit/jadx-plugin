plugins {
    kotlin("jvm") version "2.4.10"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "cc.meteormc"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.skylot:jadx-core:1.5.6")
    compileOnly("io.github.skylot:jadx-gui:1.5.6")
    compileOnly("io.github.skylot:jadx-plugins-tools:1.5.6")
}

kotlin {
    jvmToolchain(11)
}

tasks {
    jar {
        archiveFileName.set("xposedkit-extension.jar")
    }

    val shadowJar = withType(com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar::class) {
        minimize()
        archiveClassifier.set("") // remove '-all' suffix
    }

    // copy result jar into "build/dist" directory
    register<Copy>("dist") {
        group = "jadx-plugin"
        dependsOn(shadowJar)
        dependsOn(withType(Jar::class))

        from(shadowJar)
        into(layout.buildDirectory.dir("dist"))
    }
}