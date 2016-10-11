package ru.yandex.qatools.allure.command;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.commons.AllureFileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
@Command(name = "generate", description = "Generate report")
public class ReportGenerate extends ReportCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportGenerate.class);

    public static final String MAIN = "ru.yandex.qatools.allure.AllureMain";

    public static final String JAR_FILES_REGEX = ".*\\.jar";

    @Arguments(title = "Results directories", required = true,
            description = "A list of input directories to be processed")
    public List<String> results = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void runUnsafe() throws Exception {
        validateResultsDirectories();
        CommandLine commandLine = createCommandLine();
        new DefaultExecutor().execute(commandLine);
        LOGGER.info("Report successfully generated to the directory <{}>. " +
                "Use `allure report open` command to show the report.", getReportDirectoryPath());
    }

    /**
     * Throws an exception if at least one results directory is missing.
     */
    protected void validateResultsDirectories() {
        for (String result : results) {
            if (!(new File(result)).exists()) {
                throw new AllureCommandException(String.format("Report directory <%s> not found.", result));
            }
        }
    }

    /**
     * Create a {@link CommandLine} to run bundle with needed arguments.
     */
    private CommandLine createCommandLine() throws IOException {
        return new CommandLine(getJavaExecutablePath())
                .addArguments(getBundleJavaOptsArgument())
                .addArgument(getLoggerConfigurationArgument())
                .addArgument("-jar")
                .addArgument(getExecutableJar())
                .addArguments(results.toArray(new String[results.size()]), false)
                .addArgument(getReportDirectoryPath().toString(), false);
    }

    /**
     * Returns the classpath for executable jar.
     */
    protected String getClasspath() throws IOException {
        List<String> classpath = new ArrayList<>();
        classpath.add(getBundleJarPath());
        classpath.addAll(getPluginsPath());
        return StringUtils.toString(classpath.toArray(new String[classpath.size()]), " ");
    }

    /**
     * Create an executable jar to generate the report. Created jar contains only
     * allure configuration file.
     */
    protected String getExecutableJar() throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, MAIN);
        manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, getClasspath());

        File jar = new File(createTempDirectory("exec"), "generate.jar");
        try (JarOutputStream output = new JarOutputStream(new FileOutputStream(jar), manifest)) {
            output.putNextEntry(new JarEntry("allure.properties"));
            File allureConfig = PROPERTIES.getAllureConfig();
            if (allureConfig.exists()) {
                InputStream is = new FileInputStream(allureConfig);
                byte[] bytes = IOUtils.toByteArray(is);
                output.write(bytes);
            }
            output.closeEntry();
        }

        return jar.getAbsolutePath();
    }

    /**
     * Returns the bundle jar classpath element.
     */
    protected String getBundleJarPath() throws MalformedURLException {
        File path = new File(PROPERTIES.getAllureHome().getAbsolutePath(), "app/allure-bundle.jar");
        if (!path.exists()) {
            throw new AllureCommandException(String.format("Bundle not found by path <%s>", path));
        }
        return path.toURI().toString();
    }

    /**
     * Returns the plugins classpath elements.
     */
    protected List<String> getPluginsPath() {
        List<String> result = new ArrayList<>();
        File pluginsDirectory = new File(PROPERTIES.getAllureHome().getAbsolutePath(), "plugins");
        if (!pluginsDirectory.exists()) {
            return Collections.emptyList();
        }

        File[] plugins = AllureFileUtils.listFiles(pluginsDirectory, JAR_FILES_REGEX);
        if (plugins != null) {
            for (File plugin : plugins) {
                result.add(plugin.toURI().toString());
            }
        }
        return result;
    }

    /**
     * Get argument to configure log level for bundle.
     */
    protected String getLoggerConfigurationArgument() {
        return String.format("-Dorg.slf4j.simpleLogger.defaultLogLevel=%s",
                isQuiet() || !isVerbose() ? "error" : "debug");
    }

    /**
     * Returns the bundle java options split by space.
     */
    protected String getBundleJavaOptsArgument() {
        return PROPERTIES.getBundleJavaOpts();
    }

    /**
     * Returns the path to java executable.
     */
    protected String getJavaExecutablePath() {
        String executableName = isWindows() ? "bin/java.exe" : "bin/java";
        return new File(PROPERTIES.getJavaHome(), executableName).getAbsolutePath();
    }

    /**
     * Returns true if operation system is windows, false otherwise.
     */
    protected boolean isWindows() {
        return PROPERTIES.getOsName().contains("win");
    }
}
