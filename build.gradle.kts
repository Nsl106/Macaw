import org.hidetake.groovy.ssh.core.Remote
import org.hidetake.groovy.ssh.core.RunHandler
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.session.SessionHandler
import java.io.FileInputStream
import java.util.*

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.1"
    id("org.hidetake.ssh") version "2.11.2"

    kotlin("jvm") version "1.8.20"
    application
}

application.mainClass.set("dev.expo.macaw.MainKt")

repositories {
    mavenCentral()
    gradlePluginPortal()

    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-beta.3") // Java discord api (JDA)
    implementation("com.squareup.okhttp3:okhttp:4.10.0") // Http client (OkHttp)
    implementation("com.google.code.gson:gson:2.10.1") // JSON library (gson)
    implementation("org.jsonschema2pojo:jsonschema2pojo-core:1.2.1") // JSON to POJO (jsonschema2pojo)
}

kotlin { // Extension for easy setup
    jvmToolchain(17) // Target version of generated JVM bytecode
}

val config = Properties()
config.load(FileInputStream(File("src/main/kotlin/dev/expo/macaw/config/config.properties")))

val ip: String = config.getProperty("serverHost")
val username: String = config.getProperty("serverUser")
val password: String = config.getProperty("serverPassword")

val pi = Remote(
    mapOf<String, String>(
        "host" to ip,
        "user" to username,
        "password" to password
    )
)

tasks.create("deploy") {
//    val installDist by tasks.named<Sync>("installDist")
//    dependsOn(installDist)
    dependsOn(tasks.shadowJar)
    doLast {
        ssh.runSessions {
            session(pi) {
                put("${config.getProperty("projectDir")}/build/libs/Macaw-all.jar", "/home/$username/macaw")

                execute("java -jar /home/$username/macaw/Macaw-all.jar")
            }
        }
    }
}

fun Service.runSessions(action: RunHandler.() -> Unit) {
    run(delegateClosureOf(action))
}

fun RunHandler.session(vararg remotes: Remote, action: SessionHandler.() -> Unit) {
    session(*remotes, delegateClosureOf(action))
}

fun SessionHandler.put(from: Any, into: Any) {
    put(hashMapOf("from" to from, "into" to into))
}