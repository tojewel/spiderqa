package org.jenkinsci.test.acceptance.plugins.subversion;

import org.jenkinsci.test.acceptance.po.*;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Encapsulates the PageArea of the Subversion SCM
 *
 * @author Kohsuke Kawaguchi
 * @author Matthias Karl
 */
@Describable("Subversion")
public class SubversionScm extends Scm {
    public static final String ALWAYS_FRESH_COPY = "Always check out a fresh copy";
    public static final String CLEAN_CHECKOUT = "Emulate clean checkout by first deleting";

    public final Control url = control("locations/remote");
    public final Control btAdvanced = control(by.xpath("//tr[@nameref='radio-block-3']//div[@class='advancedLink']//button"));
    public final Control local = control("locations/local");
    public final Control checkoutStrategy = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select", "Check-out Strategy"));
    public final Control credentials = control("locations/credentialsId");
    public final Control repositoryBrowser = control(by.xpath("//td[@class='setting-name' and text()='%s']/../td[@class='setting-main']/select", "Repository browser"));

    /**
     * Opens the SVNPlugin credential page for protected repositories.
     * Only for plugin version 1.54 and older.
     *
     * @param type child of SubversionCredential.class
     * @param <T>  child of SubversionCredential.class
     * @return PageObject of the CredentialPage
     * @throws SubversionPluginTestException if Url to credential page is not found or malformed.
     */
    @Deprecated
    public <T extends PageObject> T getCredentialPage(Class<T> type) throws SubversionPluginTestException {
        //click into a different field to trigger the Url-Check
        this.local.click();
        URL urlOfCredentialPage = null;
        WebElement linkToCredentialPage;
        String urlString = null;
        try {
            elasticSleep(1000);
            linkToCredentialPage = this.find(by.link("enter credential"));
            urlString = linkToCredentialPage.getAttribute("href");
            urlOfCredentialPage = new URL(urlString);
            linkToCredentialPage.click();
        } catch (NoSuchElementException e) {
            SubversionPluginTestException.throwRepoMayNotBeProtected(e);
        } catch (MalformedURLException e) {
            SubversionPluginTestException.throwMalformedURL(e, urlString);
        }
        return this.newInstance(type, this.injector, urlOfCredentialPage, driver.getWindowHandle());
    }


    public <T extends SvnRepositoryBrowser> T useRepositoryBrowser(Class<T> type) {
        final String[] nameOfRepositoryBrowser = type.getAnnotation(Describable.class).value();
        repositoryBrowser.select(nameOfRepositoryBrowser[0]);
        String path = repositoryBrowser.resolve().getAttribute("path");
        return this.newInstance(type, this, this.getPage().url(path));
    }

    public SubversionSvmAdvanced advanced() {
        btAdvanced.click();
        return this.newInstance(SubversionSvmAdvanced.class, this.getPage(), this.getPage().url);
    }


    public SubversionScm(Job job, String path) {
        super(job, path);
    }


}
