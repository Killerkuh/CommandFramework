/*
 *   This file is a part of the SteamWar software.
 *
 *   Copyright (C) 2025 SteamWar.de-Serverteam
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


plugins {
    `java-library`
}

group = "de.steamwar"
version = "1.0.0"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("src"))
        }
        resources {
            setSrcDirs(listOf("src"))
            exclude("**/*.java", "**/*.kt")
        }
    }

    test {
        java {
            setSrcDirs(listOf("testsrc"))
        }
        resources {
            setSrcDirs(listOf("testsrc"))
            exclude("**/*.java", "**/*.kt")
        }
    }
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    testCompileOnly("org.projectlombok:lombok:1.18.32")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.32")

    // Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.hamcrest:hamcrest:2.2")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}
