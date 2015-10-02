//
// +-----------------------------------------------------+
// |              =========================              |
// |              !  W  A  R  N  I  N  G  !              |
// |              =========================              |
// |                                                     |
// | This file is  N O T   P A R T  of the jenkins       |
// | acceptance test harness project's source code!      |
// |                                                     |
// | This file is only used for testing purposes w.r.t   |
// | the task scanner plugin test.                       |
// |                                                     |
// +-----------------------------------------------------+
//


package org.jenkinsci.test.acceptance.po;

import com.google.inject.Injector;

import org.jenkinsci.test.acceptance.junit.Resource;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.security.acl.Owner;

/**
 * Wraps a specific form element in {@link PageArea} to provide operations.
 *
 * {@link Control} is like a {@link WebElement}, but with the following key differences:
 *
 * <ul>
 * <li>{@link Control} is late binding, and the underlying {@link WebElement} is resolved only when
 *     an interaction with control happens. This allows {@link Control}s to be instantiated earlier
 *     (typically when a {@link PageObject} subtype is instantiated.)
 * <li>{@link Control} offers richer methods to interact with a form element, making the right code easier to write.
 * </ul>
 *
 * See {@link PageArea} subtypes for typical usage.
 *
 * @author Kohsuke Kawaguchi
 * @see PageArea#control(String...)
 */
public class TSRControl extends CapybaraPortingLayer {
    private final Owner parent;
    private final String[] relativePaths;

    public Control(PageArea parent, String... relativePaths) {
        super(parent.injector);
        this.parent = parent;
        this.relativePaths = relativePaths;
    }

    /**
     * Creates a control by giving their full path in the page
     */
    public Control(PageObject parent, String... paths) {
        super(parent.injector);
        this.parent = new Owner() {
            @Override
            public By path(String rel) {
                return by.path(rel);
            }
        };
        this.relativePaths = paths;
    }

    public Control(Injector injector, final By selector) {
        super(injector);
        this.relativePaths = new String[1];
        this.parent = new Owner() {
            @Override
            public By path(String rel) {
                return selector;
            }
        };
    }

    public WebElement resolve() {
        NoSuchElementException problem = new NoSuchElementException("No relative path specified!");
        for(String p : relativePaths) {
            try {
                return find(parent.path(p));
            } catch (NoSuchElementException e) {
                problem = e;
            }
        }
        throw problem;
    }

    public void sendKeys(String t) {
        resolve().sendKeys(t);
    }

    public void uncheck() {
        check(resolve(), false);
    }

    public void check() {
        check(resolve(),true);
    }

    public void check(boolean state) {
        check(resolve(),state);
    }

    public void click() {
        resolve().click();
    }

    public void set(String text) {
        WebElement e = resolve();
        e.clear();
        e.sendKeys(text);
    }

    public void set(Object text) {
        set(text.toString());
    }

    /**
     * Clicks a menu button, and selects the matching item from the drop down
     *
     * @param type
     *      Class with {@link Describable} annotation.
     */
    public void selectDropdownMenu(Class type) {
        click();
        findCaption(type,findDropDownMenuItem).click();
        sleep(1000);
    }

    public void selectDropdownMenu(String displayName) {
        click();
        findDropDownMenuItem.find(displayName).click();
        sleep(1000);
    }

    /**
     * Given a menu button that shows a list of build steps, select the right item from the menu
     * to insert the said build step.
     */
    private Finder<WebElement> findDropDownMenuItem = new Finder<WebElement>() {
        @Override
        protected WebElement find(String caption) {
            WebElement menuButton = resolve();

            // With enough implementations registered the one we are looking for might
            // require scrolling in menu to become visible. This dirty hack stretch
            // yui menu so that all the items are visible.
            executeScript("" +
                            "YAHOO.util.Dom.batch(" +
                            "    document.querySelector('.yui-menu-body-scrolled')," +
                            "    function (el) {" +
                            "        el.style.height = 'auto';" +
                            "        YAHOO.util.Dom.removeClass(el, 'yui-menu-body-scrolled');" +
                            "    }" +
                            ");"
            );

            WebElement context = menuButton.findElement(by.xpath("ancestor::*[contains(@class,'yui-menu-button')]/.."));
            WebElement e = context.findElement(by.link(caption));
            return e;
        }
    };

    /**
     * Select an option.
     */
    public void select(String option) {
        WebElement e = resolve();
        e.findElement(by.option(option)).click();

        // move the focus away from the select control to fire onchange event
        e.sendKeys(Keys.TAB);
    }

    public void upload(Resource res) {
        resolve().sendKeys(res.asFile().getAbsolutePath());
    }

    public interface Owner {
        /**
         * Resolves relative path into a selector.
         */
        By path(String rel);
    }
}
