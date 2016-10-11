package ru.yandex.qatools.allure;

import ru.qatools.properties.DefaultValue;
import ru.qatools.properties.Property;
import ru.qatools.properties.Required;
import ru.qatools.properties.Resource;

import java.io.File;

/**
 * @author Artem Eroshenko <eroshenkoam@yandex-team.ru>
 */
@Resource.Classpath({"command.properties"})
public interface CommandProperties {

    @Required
    @Property("os.name")
    String getOsName();

    @Required
    @Property("java.home")
    File getJavaHome();

    @Required
    @Property("allure.home")
    File getAllureHome();

    @DefaultValue("allure.properties")
    @Property("allure.config")
    File getAllureConfig();

    @DefaultValue("-Xms128m")
    @Property("allure.bundle.javaOpts")
    String getBundleJavaOpts();

}
