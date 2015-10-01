/*
 * The MIT License
 *
 * Copyright (c) 2014 Sony Mobile Communications Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package core;

import com.google.inject.Inject;
import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.Slave;
import org.jenkinsci.test.acceptance.slave.SlaveController;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.ExecutionException;

/**
 * Feature: Slave management tests.
 * @author Orjan Percy <orjan.percy@sonymobile.com>
 */
public class SlaveTest extends AbstractJUnitTest {
    @Inject
    SlaveController slaveController;
    Slave slave;

    @Before
    public void setUp() throws ExecutionException, InterruptedException {
        slave = slaveController.install(jenkins).get();
    }

    /**
     * Scenario: Bring slave offline and then online.
     */
    @Test
    public void slave_offline_online() {
        slave.markOffline("Test - slave goes offline.");
        assert(slave.isOffline());
        slave.markOnline();
        assert(slave.isOnline());
    }

    /**
     * Scenario: Disconnect a slave, logout - login and then reconnect the slave.
     */
    @Test
    public void slave_disconnect_reconnect() throws ExecutionException, InterruptedException {
        slave.disconnect("Test - slave is disconnected");
        assert(slave.isOffline());
        jenkins.logout();
        jenkins.login();
        slave.launchSlaveAgent();
        slave.waitUntilOnline();
        assert(slave.isOnline());
    }
}
