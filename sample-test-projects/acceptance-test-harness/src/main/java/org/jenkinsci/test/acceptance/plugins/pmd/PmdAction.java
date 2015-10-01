package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisAction;
import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisSettings;
import org.jenkinsci.test.acceptance.po.Build;
import org.jenkinsci.test.acceptance.po.Job;

/**
 * Page object for PMD action.
 *
 * @author Fabian Trampusch
 */
public class PmdAction extends AnalysisAction {
    private static final String PLUGIN = "pmd";

    public PmdAction(final Build parent) {
        super(parent, PLUGIN);
    }

    public PmdAction(final Job parent) {
        super(parent, PLUGIN);
    }

    @Override
    public String getName() {
        return "PMD";
    }

    @Override
    public Class<? extends AnalysisSettings> getFreeStyleSettings() {
        return PmdFreestyleSettings.class;
    }
}
