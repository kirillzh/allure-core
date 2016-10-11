package ru.yandex.qatools.allure.data;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import ru.yandex.qatools.allure.commons.AllureFileUtils;
import ru.yandex.qatools.allure.data.io.ReportWriter;
import ru.yandex.qatools.allure.data.plugins.PluginManager;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 19.02.15
 */
public class AllureReportGeneratorTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();


    @Test
    public void shouldGenerateWithoutFailures() throws Exception {
        AllureReportGenerator generator = new AllureReportGenerator(new File("target/allure-results"));
        File outputDirectory = folder.newFolder();
        generator.generate(outputDirectory);

        File dataDirectory = new File(outputDirectory, ReportWriter.DATA_DIRECTORY_NAME);
        assertTrue("Data directory should be created", dataDirectory.exists());

        assertTrue(new File(dataDirectory, "xunit.json").exists());
        assertTrue(new File(dataDirectory, "timeline.json").exists());
        assertTrue(new File(dataDirectory, "behaviors.json").exists());
        assertTrue(new File(dataDirectory, "defects.json").exists());
        assertTrue(new File(dataDirectory, "graph.json").exists());
        assertTrue(new File(dataDirectory, PluginManager.WIDGETS_JSON).exists());
        assertTrue(new File(dataDirectory, ReportWriter.REPORT_JSON).exists());

        File[] attachments = AllureFileUtils.listFiles(dataDirectory, ".*-attachment.*");
        assertNotNull(attachments);
        assertEquals(attachments.length, 14);

        File[] testcases = AllureFileUtils.listFiles(dataDirectory, ".*-testcase\\.json");
        assertNotNull(testcases);
        assertEquals(testcases.length, 319);
    }

    @Test(expected = ReportGenerationException.class)
    public void shouldFailIfNoResults() throws Exception {
        AllureReportGenerator generator = new AllureReportGenerator(folder.newFolder());
        generator.generate(folder.newFolder());
    }

    @Test(expected = ReportGenerationException.class)
    public void shouldFailIfNoResultsDirectory() throws Exception {
        AllureReportGenerator generator = new AllureReportGenerator(
                new File(folder.newFolder(), "unknown-directory"));
        generator.generate(folder.newFolder());
    }
}
