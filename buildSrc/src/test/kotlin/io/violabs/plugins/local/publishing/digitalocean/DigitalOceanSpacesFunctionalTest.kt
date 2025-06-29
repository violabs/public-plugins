//package io.violabs.plugins.local.publishing.digitalocean
//
//import org.gradle.testkit.runner.GradleRunner
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.io.TempDir
//import java.io.File
//import java.nio.file.Files
//
////public class BuildLogicFunctionalTest {
////
////    @TempDir File testProjectDir;
////    private File settingsFile;
////    private File buildFile;
////
////    @BeforeEach
////    public void setup() {
////        settingsFile = new File(testProjectDir, "settings.gradle");
////        buildFile = new File(testProjectDir, "build.gradle");
////    }
////
////    @Test
////    public void testHelloWorldTask() throws IOException {
////        writeFile(settingsFile, "rootProject.name = 'hello-world'");
////        String buildFileContent = "task helloWorld {" +
////                                  "    doLast {" +
////                                  "        println 'Hello world!'" +
////                                  "    }" +
////                                  "}";
////        writeFile(buildFile, buildFileContent);
////
////        BuildResult result = GradleRunner.create()
////            .withProjectDir(testProjectDir)
////            .withArguments("helloWorld")
////            .build();
////
////        assertTrue(result.getOutput().contains("Hello world!"));
////        assertEquals(SUCCESS, result.task(":helloWorld").getOutcome());
////    }
////
////    private void writeFile(File destination, String content) throws IOException {
////        BufferedWriter output = null;
////        try {
////            output = new BufferedWriter(new FileWriter(destination));
////            output.write(content);
////        } finally {
////            if (output != null) {
////                output.close();
////            }
////        }
////    }
////}
//class DigitalOceanSpacesFunctionalTest {
//    @TempDir
//    lateinit var testProjectDir: File
//    private lateinit var settingsFile: File
//    private lateinit var buildFile: File
//
//    @BeforeEach
//    fun setup() {
//        this.settingsFile = File(testProjectDir, "settings.gradle.kts")
//        this.buildFile = File(testProjectDir, "build.gradle.kts")
//    }
//
//    @Test
//    fun `plugin uploads jar using mock S3 client`() {
//        // 1. Create a temporary Gradle project for testing
//        val testProjectDir = Files.createTempDirectory("gradle-test").toFile()
//        val buildFile = File(testProjectDir, "build.gradle.kts")
//        val settingsFile = File(testProjectDir, "settings.gradle.kts")
//        val pluginClasspathResource = this::class.java.classLoader.getResource("plugin-classpath.txt")
//            ?: throw IllegalStateException("Did not find plugin classpath resource. Run `test` task first.")
//        val pluginClasspath = pluginClasspathResource.readText().lines().filter { it.isNotBlank() }.map { File(it) }
//
//        // 2. Write a build script that applies your plugin and injects a mock client
//        buildFile.writeText("""
//            plugins {
//                id("io.violabs.plugins.local.publishing.digitalocean")
//            }
//
//            import io.violabs.plugins.local.publishing.digitalocean.DigitalOceanSpacesClient
//
//            tasks.withType<io.violabs.plugins.local.publishing.digitalocean.DigitalOceanSpacesUploadTask> {
//                // Here is the magic! We inject a mock object
//                digitalOceanSpacesClient = project.ext["mockSpacesClient"] as DigitalOceanSpacesClient
//                checkS3Client = project.ext["mockS3Client"] as software.amazon.awssdk.services.s3.S3Client
//                jarQualifier = "test"
//                isPlugin = false
//            }
//        """.trimIndent())
//        settingsFile.writeText("""rootProject.name = "functional-test-project"""")
//
//        // 3. Create a dummy JAR file to upload
//        val libsDir = File(testProjectDir, "build/libs")
//        libsDir.mkdirs()
//        File(libsDir, "functional-test-project-1.0.0.jar").writeText("dummy-jar-content")
//
//        // 4. Place a dummy pom file too, if needed
//        val publicationsDir = File(testProjectDir, "build/publications/maven")
//        publicationsDir.mkdirs()
//        File(publicationsDir, "pom-default.xml").writeText("<project/>")
//
//        // 5. Prepare your mock and attach it to the build via project.ext
//        // This requires a test-only plugin or special scripting. Here's a practical cheat:
//        // Use Java serialization for simple objects or write a mini buildSrc helper.
//        // Instead, here's the accepted approach: test your upload method calls a static flag or sets a file.
//
//        // 6. Run the build using GradleRunner
//        val result = GradleRunner.create()
//            .withProjectDir(testProjectDir)
//            .withArguments("digitalOceanSpacesUpload")
//            .withPluginClasspath(pluginClasspath)
//            .withEnvironment(mapOf(
//                // Optionally pass test flags via env
//            ))
//            .forwardOutput()
//            .build()
//
//        // 7. Assert the build succeeded
//        assertTrue(result.output.contains("BUILD SUCCESS"), "Build did not succeed")
//
//        // 8. (If using a mock): Check that the dummy upload target (file, log, etc.) exists
//        // In a real-world project, you would write your uploadFile method to check for a special flag or file for test mode,
//        // or use dependency injection to swap in a mock implementation and log to file.
//
//        // CLEANUP
//        testProjectDir.deleteRecursively()
//    }
//}