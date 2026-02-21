
plugins {    
    application
}

repositories {    
    mavenCentral()
}

dependencies {    
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.mockito.core)

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    
    implementation(libs.guava)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.mybatis.spring.boot.starter)
    runtimeOnly("com.h2database:h2:2.2.224")

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {    
    mainClass = "mobilefactory.App"
}

tasks.named<Test>("test") {    
    useJUnitPlatform()
}
