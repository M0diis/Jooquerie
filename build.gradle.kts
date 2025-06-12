import org.jooq.util.jaxb.tools.XMLAppendable

plugins {
    id("java")
    id("groovy")
    id("idea")
    id("nu.studer.jooq") version "10.1"
    id("org.liquibase.gradle") version "2.2.2"
}

group = "me.m0dii"
version = "1.0.0"

repositories {
    mavenCentral()
}

val jooqVersion = "3.19.8"
val h2Version = "2.3.232"
val liquibaseVersion = "4.20.0"
val spockVersion = "2.4-M1-groovy-4.0"
val lombokVersion = "1.18.30"
val mockitoVersion = "4.8.0"
val assertjVersion = "3.22.0"
val junitVersion = "5.10.0"
val logbackVersion = "1.5.18"
val picocliVersion = "4.6.1"

dependencies {
    implementation("org.projectlombok:lombok:$lombokVersion")

    // Liquibase for database migrations
    implementation("org.liquibase:liquibase-core:$liquibaseVersion")

    // HikariCP
    implementation("com.zaxxer:HikariCP:5.0.1")

    // H2 Database
    implementation("com.h2database:h2:$h2Version")

    runtimeOnly("com.h2database:h2:$h2Version")

    jooqGenerator("com.h2database:h2:$h2Version")
    jooqGenerator("org.jooq:jooq:$jooqVersion")
    jooqGenerator("org.jooq:jooq-meta:$jooqVersion")
    jooqGenerator("org.jooq:jooq-codegen:$jooqVersion")

    // Liquibase runtime dependencies
    liquibaseRuntime("org.liquibase:liquibase-core:$liquibaseVersion")
    liquibaseRuntime("com.h2database:h2:$h2Version")
    // Needed for liquibase to run tasks
    liquibaseRuntime("ch.qos.logback:logback-core:$logbackVersion")
    liquibaseRuntime("ch.qos.logback:logback-classic:$logbackVersion")
    liquibaseRuntime("info.picocli:picocli:$picocliVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("org.spockframework:spock-core:$spockVersion")
    testImplementation("org.spockframework:spock-spring:$spockVersion")

    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("org.mockito:mockito-core:$mockitoVersion")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
    testImplementation("org.assertj:assertj-core:$assertjVersion")
    testImplementation("com.h2database:h2:$h2Version")

    annotationProcessor("org.projectlombok:lombok:$lombokVersion")
}

jooq {
    configurations {
        create("main") {
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.h2.Driver"
                    url = "jdbc:h2:mem:testdb"
                    user = "sa"
                    password = ""
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    database.apply {
                        name = "org.jooq.meta.h2.H2Database"
                        inputSchema = "PUBLIC"
                    }
                    generate.apply {
                        isPojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "me.m0dii.jooquerie.generated"
                        directory = "build/generated-src/jooq/main"
                    }
                }
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    exclude("me/m0dii/jooquerie/example/**")
}

operator fun <T : XMLAppendable> T.invoke(block: T.() -> Unit) = this.apply(block)