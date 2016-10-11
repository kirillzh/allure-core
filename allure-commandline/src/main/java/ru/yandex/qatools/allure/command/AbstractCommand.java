package ru.yandex.qatools.allure.command;

import io.airlift.airline.Option;
import io.airlift.airline.OptionType;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.qatools.properties.PropertyLoader;
import ru.yandex.qatools.allure.CommandProperties;
import ru.yandex.qatools.allure.commons.AllureFileUtils;

import java.io.File;
import java.io.IOException;

import static ru.yandex.qatools.allure.command.ExitCode.GENERIC_ERROR;
import static ru.yandex.qatools.allure.command.ExitCode.NO_ERROR;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
public abstract class AbstractCommand implements AllureCommand {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCommand.class);

    private ExitCode exitCode = NO_ERROR;

    private File tempDirectory;

    @Option(name = {"-v", "--verbose"}, type = OptionType.GLOBAL,
            description = "Switch on the verbose mode.")
    protected boolean verbose = false;

    @Option(name = {"-q", "--quiet"}, type = OptionType.GLOBAL,
            description = "Switch on the quiet mode.")
    protected boolean quiet = false;

    protected static final CommandProperties PROPERTIES =
            PropertyLoader.newInstance().populate(CommandProperties.class);

    protected abstract void runUnsafe() throws Exception; //NOSONAR

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        setLogLevel();
        try {
            initTempDirectory();
            runUnsafe();
        } catch (Exception e) {
            LOGGER.error("Command aborted due to exception {}.", e);
            setExitCode(GENERIC_ERROR);
        } finally {
            removeTempDirectory();
        }
    }

    /**
     * Creates an temporary directory. The created directory will be deleted when
     * command will ended.
     */
    File createTempDirectory(String prefix) {
        try {
            return AllureFileUtils.createTempDirectory(tempDirectory, prefix);
        } catch (IOException e) {
            throw new AllureCommandException(e);
        }
    }

    /**
     * Init {@link #tempDirectory}.
     */
    private void initTempDirectory() {
        try {
            tempDirectory = AllureFileUtils.createTempDirectory("allure-commandline");
        } catch (IOException e) {
            throw new AllureCommandException(e);
        }
    }

    /**
     * Safe remove {@link #tempDirectory}.
     */
    private void removeTempDirectory() {
        if (tempDirectory != null && tempDirectory.exists()) {
            try {
                AllureFileUtils.deleteDirectory(tempDirectory);
                LOGGER.debug("Temp directory was successfully cleaned");
            } catch (IOException e) {
                LOGGER.debug("Could not clean temp directory");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExitCode getExitCode() {
        return exitCode;
    }

    /**
     * Set the command exit code.
     *
     * @see ExitCode
     */
    protected void setExitCode(ExitCode exitCode) {
        this.exitCode = exitCode;
    }

    /**
     * Returns true if silent mode is enabled, false otherwise.
     */
    public boolean isQuiet() {
        return quiet;
    }

    /**
     * Returns true if verbose mode is enabled, false otherwise.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Configure logger with needed level {@link #getLogLevel()}.
     */
    private void setLogLevel() {
        LogManager.getRootLogger().setLevel(getLogLevel());
    }

    /**
     * Get log level depends on provided client parameters such as verbose and quiet.
     */
    private Level getLogLevel() {
        return isQuiet() ? Level.OFF : isVerbose() ? Level.DEBUG : Level.INFO;
    }

}
