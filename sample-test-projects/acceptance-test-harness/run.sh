#!/bin/bash


if [ $# -lt 2 ]; then
	cat <<USAGE
Usage: $0 BROWSER JENKINS [ARGS]

The script runs dryrun tests first to discover trivial problems immediately.
It can use jenkins.war from local maven repository or download it when missing.

BROWSER: Value for BROWSER variable
JENKINS: Path to the jenkins.war, Jenkins version of one of "latest", "latest-rc", "lts" and "lts-rc"

Examples:

# Run full suite in FF against ./jenkins.war.
$ ./run firefox ./jenkins.war

# Run Ant plugin test in chrome against Jenkins 1.512.
$ ./run chrome 1.512 -Dtest=AntPluginTest

# Run full suite in FF against LTS release candidate
$ ./run firefox lts-rc
USAGE
  exit -2
fi

browser=$1
war=$2
if [ ! -f $war ]; then
    mirrors=http://mirrors.jenkins-ci.org
    case "$war" in
        "latest")
            war=jenkins-latest.war
            url=$mirrors/war/latest/jenkins.war
        ;;
        "latest-rc")
            war=jenkins-latest-rc.war
            url=$mirrors/war-rc/latest/jenkins.war
        ;;
        "lts")
            war=jenkins-lts.war
            url=$mirrors/war-stable/latest/jenkins.war
        ;;
        "lts-rc")
            war=jenkins-lts-rc.war
            url=$mirrors/war-stable-rc/latest/jenkins.war
        ;;
    esac

    if [ -n "$url" ]; then
        find $war -maxdepth 0 -mtime +1 -delete 2> /dev/null
        if [ ! -f $war ]; then
            echo "Fetching $war"
            curl -sL -o $war $url
        fi
    fi
fi

if [ ! -f $war ] && [[ $war == *.war ]]; then
    curl -sL -o jenkins.war $war && war=jenkins.war
fi

if [ ! -f $war ]; then

	wardir=~/.m2/repository/org/jenkins-ci/main/jenkins-war

    war=$wardir/$2/jenkins-war-$2.war
    if [ ! -f $war ]; then

        mvn org.apache.maven.plugins:maven-dependency-plugin:2.7:get\
            -DremoteRepositories=repo.jenkins-ci.org::::http://repo.jenkins-ci.org/public/\
            -Dartifact=org.jenkins-ci.main:jenkins-war:$2:war
    fi

    if [ ! -f $war ]; then

        echo "No such jenkins.war. Available local versions:"
        ls $wardir/*/jenkins-war-*.war | sed -r -e 's/.*jenkins-war-(.+)\.war/\1/'
        exit -1
    fi
fi

shift 2

set -x

BROWSER=$browser JENKINS_WAR=$war mvn test "$@"
