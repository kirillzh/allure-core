package ru.yandex.qatools.allure.command;

import io.airlift.airline.Option;
import io.airlift.airline.OptionType;

import java.io.File;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
public abstract class ReportCommand extends AbstractCommand {

    @Option(name = {"-o", "--report-dir", "--output"}, type = OptionType.COMMAND,
            description = "The directory to generate Allure report into.")
    protected String reportDirectory = "allure-report";

    /**
     * The string representation of path to the report directory.
     */
    protected String getReportDirectory() {
        return reportDirectory;
    }

    /**
     * The path to the report directory {@link #getReportDirectory()}.
     */
    protected File getReportDirectoryPath() {
        return new File(getReportDirectory());
    }
}
