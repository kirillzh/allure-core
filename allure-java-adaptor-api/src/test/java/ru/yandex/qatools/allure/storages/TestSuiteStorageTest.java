package ru.yandex.qatools.allure.storages;

import org.junit.Before;
import org.junit.Test;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import static org.junit.Assert.*;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 14.12.13
 */
public class TestSuiteStorageTest {

    private TestSuiteStorage testSuiteStorage;

    @Before
    public void setUp() throws Exception {
        testSuiteStorage = new TestSuiteStorage();
    }

    @Test
    public void getTest() throws Exception {
        TestSuiteResult testSuite = testSuiteStorage.get("a");
        assertSame(testSuite, testSuiteStorage.get("a"));
        assertNotSame(testSuite, testSuiteStorage.get("b"));
    }

    @Test
    public void removeTest() throws Exception {
        TestSuiteResult testSuite = testSuiteStorage.get("a");
        assertSame(testSuite, testSuiteStorage.get("a"));
        testSuiteStorage.remove("a");
        assertNotSame(testSuite, testSuiteStorage.get("a"));
    }
}
