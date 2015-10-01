/*
 * The MIT License
 *
 * Copyright (c) 2014 Red Hat, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.jenkinsci.test.acceptance.junit.AbstractJUnitTest;
import org.jenkinsci.test.acceptance.po.GlobalSecurityConfig;
import org.jenkinsci.test.acceptance.po.JenkinsDatabaseSecurityRealm;
import org.jenkinsci.test.acceptance.po.User;
import org.junit.Before;
import org.junit.Test;

public class JenkinsDatabaseSecurityRealmTest extends AbstractJUnitTest {

    private JenkinsDatabaseSecurityRealm realm;

    @Before
    public void setUp() {
        GlobalSecurityConfig security = new GlobalSecurityConfig(jenkins);
        security.configure();
        realm = security.useRealm(JenkinsDatabaseSecurityRealm.class);
        security.save();
    }

    @Test
    public void login_and_logout() {

        User user = realm.signup("jenkins-acceptance-tests-user");

        assertTrue("User should exist", !new User(jenkins, "jenkins-acceptance-tests-userx").mail().isEmpty());

        jenkins.login().doLogin(user);

        assertEquals(user, jenkins.getCurrentUser());

        jenkins.logout();

        assertEquals(null, jenkins.getCurrentUser());
    }
}
