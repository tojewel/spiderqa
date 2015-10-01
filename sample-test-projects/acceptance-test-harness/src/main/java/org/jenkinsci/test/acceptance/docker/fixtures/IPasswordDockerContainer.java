package org.jenkinsci.test.acceptance.docker.fixtures;

import org.jenkinsci.test.acceptance.docker.DockerContainer;
import org.jenkinsci.test.acceptance.docker.DockerFixture;

/**
 * gets username and password for a service on a docker container
 *
 * @author Tobias Meyer
 */
public interface IPasswordDockerContainer {
    /**
     * Gets the passsword for a service on the docker server
     *
     * @return password
     */
    public String getPassword() ;
    /**
     * Gets the username for a service on the docker server
     *
     * @return username
     */
    public String getUsername() ;

}
