package org.jenkinsci.test.acceptance.plugins.pmd;

import org.jenkinsci.test.acceptance.plugins.analysis_core.AnalysisMavenSettings;
import org.jenkinsci.test.acceptance.plugins.maven.MavenModuleSet;
import org.jenkinsci.test.acceptance.po.Describable;

/**
 * PMD build settings for maven projects.
 *
 * @author Fabian Trampusch
 */
@Describable("Publish PMD analysis results")
public class PmdMavenSettings extends AnalysisMavenSettings {
    public PmdMavenSettings(final MavenModuleSet parent, final String selectorPath) {
        super(parent, selectorPath);
    }
}
