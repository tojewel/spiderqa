package org.jenkinsci.test.acceptance.geb;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

public class GebSpec
  extends geb.spock.GebSpec {
@org.junit.Rule() public org.jenkinsci.test.acceptance.geb.GebBrowserRule browserRule;
@org.junit.Rule() public org.jenkinsci.test.acceptance.junit.JenkinsAcceptanceTestRule env;
@javax.inject.Inject() public org.jenkinsci.test.acceptance.po.Jenkins jenkins;
@javax.inject.Inject() public com.google.inject.Injector injector;
public  org.jenkinsci.test.acceptance.junit.Resource resource(java.lang.String path) { return (org.jenkinsci.test.acceptance.junit.Resource)null;}
@java.lang.Override() public  geb.Browser createBrowser() { return (geb.Browser)null;}
}
