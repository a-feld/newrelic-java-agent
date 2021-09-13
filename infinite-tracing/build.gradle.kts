import com.google.protobuf.gradle.*

plugins {
    id("com.google.protobuf") version "0.8.8"
    id("java-library")
}

group = "com.newrelic.agent.java"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    disableAutoTargetJvm()
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:3.17.3")
    implementation("io.grpc:grpc-stub:1.40.1")
    implementation("io.grpc:grpc-protobuf:1.40.1")
    implementation("io.grpc:grpc-services:1.40.1")
    if (JavaVersion.current().isJava9Compatible) {
        // Workaround for @javax.annotation.Generated
        // see: https://github.com/grpc/grpc-java/issues/3633
        compile("javax.annotation:javax.annotation-api:1.3.1")
    }

    implementation("com.newrelic.agent.java:infinite-tracing-protobuf:3.2")
    implementation("com.google.guava:guava:30.1.1-jre")
    implementation(project(":agent-model"))
    implementation(project(":agent-interfaces"))
    implementation(project(":newrelic-api"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.2")
    testImplementation("org.mockito:mockito-junit-jupiter:3.3.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.2")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.SHORT
    }
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.17.3"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.40.1"
        }
    }
    generateProtoTasks {
        ofSourceSet("main").forEach {
            it.plugins {
                // Apply the "grpc" plugin whose spec is defined above, without options.
                id("grpc")
            }
        }
    }
}