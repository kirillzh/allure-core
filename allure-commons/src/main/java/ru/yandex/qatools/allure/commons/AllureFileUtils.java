package ru.yandex.qatools.allure.commons;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.CanReadFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import ru.yandex.qatools.allure.config.AllureConfig;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import javax.xml.bind.JAXB;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Some file utils using mainly in tests. Class contains methods that can help to find attachments and
 * allures xml in given directories.
 *
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 06.06.14
 */
public final class AllureFileUtils {

    AllureFileUtils() {
        throw new IllegalStateException();
    }

    /**
     * Unmarshal test suite from given file.
     */
    public static TestSuiteResult unmarshal(File testSuite) throws IOException {
        try (InputStream stream = new FileInputStream(testSuite)) {
            return unmarshal(stream);
        }
    }

    /**
     * Unmarshal test suite from given input stream.
     *
     * @see #unmarshal(Reader)
     */
    public static TestSuiteResult unmarshal(InputStream testSuite) {
        return unmarshal(new InputStreamReader(testSuite, StandardCharsets.UTF_8));
    }

    /**
     * Unmarshal test suite from given reader.
     *
     * @see BadXmlCharacterFilterReader
     */
    public static TestSuiteResult unmarshal(Reader testSuite) {
        return JAXB.unmarshal(new BadXmlCharacterFilterReader(testSuite), TestSuiteResult.class);
    }

    /**
     * Find and unmarshal all test suite files in given directories.
     *
     * @throws IOException if any occurs.
     * @see #unmarshal(File)
     */
    public static List<TestSuiteResult> unmarshalSuites(File... directories) throws IOException {
        List<TestSuiteResult> results = new ArrayList<>();

        List<File> files = listTestSuiteFiles(directories);

        for (File file : files) {
            results.add(unmarshal(file));
        }
        return results;
    }

    /**
     * Returns list of files matches {@link AllureConfig#testSuiteFileRegex} in specified directories
     *
     * @param directories to find
     * @return list of testSuite files in specified directories
     */
    public static List<File> listTestSuiteFiles(File... directories) {
        return listFilesByRegex(
                AllureConfig.newInstance().getTestSuiteFileRegex(),
                directories
        );
    }

    /**
     * Returns list of files matches {@link AllureConfig#attachmentFileRegex} in specified directories
     *
     * @param directories to find
     * @return list of attachment files in specified directories
     */
    public static List<File> listAttachmentFiles(File... directories) {
        return listFilesByRegex(
                AllureConfig.newInstance().getAttachmentFileRegex(),
                directories
        );
    }

    /**
     * Returns list of files matches specified regex in specified directories
     *
     * @param regex       to match file names
     * @param directories to find
     * @return list of files matches specified regex in specified directories
     */
    public static List<File> listFilesByRegex(String regex, File... directories) {
        return listFiles(directories,
                new RegexFileFilter(regex),
                CanReadFileFilter.CAN_READ);
    }

    /**
     * Returns list of files matches filters in specified directories
     *
     * @param directories which will using to find files
     * @param fileFilter  file filter
     * @param dirFilter   directory filter
     * @return list of files matches filters in specified directories
     */
    public static List<File> listFiles(File[] directories, IOFileFilter fileFilter, IOFileFilter dirFilter) {
        List<File> files = new ArrayList<>();
        for (File directory : directories) {
            if (!directory.isDirectory()) {
                continue;
            }
            Collection<File> filesInDirectory = FileUtils.listFiles(directory,
                    fileFilter,
                    dirFilter);
            files.addAll(filesInDirectory);
        }
        return files;
    }

    public static void deleteDirectory(File directory) throws IOException {
        if (directory != null && directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }

            if (!directory.delete()) {
                throw new IOException("Failed to delete " + directory);
            }
        }
    }

    public static File createTempDirectory(String prefix) throws IOException {
        return createTempDirectory(null, prefix);
    }

    public static File createTempDirectory(File dir, String prefix) throws IOException {
        if (dir == null) {
            dir = new File(System.getProperty("java.io.tmpdir"));
        }

        String baseName = String.format("%s%s", prefix, System.nanoTime());

        File tempDir = new File(dir, baseName);
        if (tempDir.mkdir()) {
            return tempDir;
        }
        throw new IOException("Failed to create temporary directory " + tempDir.toString());
    }

    public static void createParentDirs(File file) throws IOException {
        File parent = file.getCanonicalFile().getParentFile();
        if (parent == null) {
            return;
        }
        parent.mkdirs();
        if (!parent.isDirectory()) {
            throw new IOException("Unable to create parent directories of " + file);
        }
    }

    public static File[] listFiles(File directory, final String regex) {
        return directory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(regex);
            }
        });
    }
}
