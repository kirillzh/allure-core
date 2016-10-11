package ru.yandex.qatools.allure.command;

import io.airlift.airline.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yandex.qatools.allure.commons.AllureFileUtils;

import java.io.File;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
@Command(name = "clean", description = "Clean report")
public class ReportClean extends ReportCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportClean.class);

    /**
     * Remove the report directory.
     */
    @Override
    protected void runUnsafe() throws Exception {
        File reportDirectory = getReportDirectoryPath();
        AllureFileUtils.deleteDirectory(reportDirectory);
        LOGGER.info("Report directory <{}> was successfully cleaned.", reportDirectory);
    }
}
