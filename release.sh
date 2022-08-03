#!/bin/sh

set -e

mvn clean

export MAVEN_OPTS="--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.text=ALL-UNNAMED --add-opens=java.desktop/java.awt.font=ALL-UNNAMED"

# Because we have a bunch of test projects that disable the nexus plugin for themselves, we must
# run with skipLocalStaging - which gets disabled by parsing test-projects/pom.xml - and we need
# to ensure the parent pom gets published regardless.  So...

mvn -fae -DdetectBuildFailures=false -DskipLocalStaging=true -Dmaven.test.skip.exec=true --activate-profiles release clean install javadoc:jar source:jar gpg:sign nexus-staging:deploy
