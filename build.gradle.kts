plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
	id("com.modrinth.minotaur") version "2.10.0"
}

repositories {
	maven("https://maven.isxander.dev/releases") {
		name = "Xander"
	}
	maven("https://maven.terraformersmc.com/releases/") {
		name = "TerraformersMC"
	}
	exclusiveContent {
		forRepository {
			maven("https://api.modrinth.com/maven") {
				name = "Modrinth"
			}
		}
		filter {
			includeGroup("maven.modrinth")
		}
	}
}

dependencies {
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")

	val yacl = "dev.isxander:yet-another-config-lib:${providers.gradleProperty("yacl_version").get()}"
	val modMenu = "com.terraformersmc:modmenu:${providers.gradleProperty("modmenu_version").get()}"
	val sodium = "maven.modrinth:sodium:${providers.gradleProperty("sodium_version").get()}"

	compileOnly(yacl)
	compileOnly(modMenu)
	compileOnly(sodium)

	localRuntime(yacl)
	localRuntime(modMenu)
	localRuntime(sodium)
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

modrinth {
	projectId = "P8dOpO3T"
	versionNumber = project.version.toString()
	versionName = "Ahead ${project.version}"
	versionType = "release"
	changelog = providers.fileContents(layout.projectDirectory.file("CHANGELOG.md")).asText
	uploadFile.set(tasks.jar)
	gameVersions.add(providers.gradleProperty("minecraft_version"))
	loaders.add("fabric")
	environment = "client_only"
	debugMode = providers.environmentVariable("MODRINTH_DEBUG").map { it.toBoolean() }.orElse(false)

	required.project("fabric-api")
	optional.project("sodium", "modmenu", "yacl")
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {
	}
}
