package org.jenkinsci.test.acceptance.po;

/**
 * @author Kohsuke Kawaguchi
 */
public class TimerTrigger extends Trigger {
    public final Control spec = control("pec");

    public TimerTrigger(Job parent) {
        super(parent, "/hudson-triggers-TimerTrigger");
    }
}
