package org.jenkinsci.test.acceptance.plugins.timestamper;

import org.jenkinsci.test.acceptance.po.Control;
import org.jenkinsci.test.acceptance.po.Jenkins;
import org.jenkinsci.test.acceptance.po.PageAreaImpl;

/**
 * @author Kohsuke Kawaguchi
 */
public class TimstamperGlobalConfig extends PageAreaImpl {
    public final Control systemTimeFormat = control("systemTimeFormat");
    public final Control elapsedTimeFormat = control("elapsedTimeFormat");

    public TimstamperGlobalConfig(Jenkins jenkins) {
        super(jenkins, "/hudson-plugins-timestamper-TimestamperConfig");
    }
}
