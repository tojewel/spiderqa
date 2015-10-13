package my.company;

import my.company.steps.WebDriverSteps;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Dmitry Baev charlie@yandex-team.ru
 *         Date: 24.11.13
 */
public class SearchTest {

    private WebDriverSteps steps;

    @BeforeMethod
    public void setUp() throws Exception {
        steps = new WebDriverSteps(
                new PhantomJSDriver(new DesiredCapabilities())
        );
    }


    @Test
    public void searchTest() throws Exception {
        steps.openMainPage();
        steps.search("Yandex QATools");
        steps.makeScreenshot();
        steps.quit();
    }
}
