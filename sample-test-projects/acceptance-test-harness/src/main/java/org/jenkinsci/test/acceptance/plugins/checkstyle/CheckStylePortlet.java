package org.jenkinsci.test.acceptance.plugins.checkstyle;

import org.jenkinsci.test.acceptance.plugins.dashboard_view.AbstractDashboardViewPortlet;
import org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * A Checkstyle portlet for {@link org.jenkinsci.test.acceptance.plugins.dashboard_view.DashboardView}.
 *
 * @author Fabian Trampusch
 */
@Describable("Checkstyle warnings per project")
public class CheckStylePortlet extends AbstractDashboardViewPortlet {
    public CheckStylePortlet(final DashboardView parent, final String path) {
        super(parent, path);
    }

}
