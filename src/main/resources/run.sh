#!/bin/bash

# Copyright 2020-2020 Equinix, Inc
# Copyright 2014-2020 The Billing Project, LLC
#
# The Billing Project licenses this file to you under the Apache License, version 2.0
# (the "License"); you may not use this file except in compliance with the License.
# You may obtain a copy of the License at:
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
# License for the specific language governing permissions and limitations
# under the License.

# Define Maven download command
MVN_DOWNLOAD="mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.2:get -DremoteRepositories=https://repo.maven.apache.org/maven2"

# Set versions for dependencies
JOOQ_VERSION=3.13.4
REACTIVE_STREAM_VERSION=1.0.2
MYSQL_VERSION=8.0.21
JAXB_VERSION=2.3.1
POSTGRESQL_VERSION=42.5.0

# Download dependencies using Maven
$MVN_DOWNLOAD -Dartifact=org.jooq:jooq:$JOOQ_VERSION:jar
$MVN_DOWNLOAD -Dartifact=org.jooq:jooq-meta:$JOOQ_VERSION:jar
$MVN_DOWNLOAD -Dartifact=org.jooq:jooq-codegen:$JOOQ_VERSION:jar
$MVN_DOWNLOAD -Dartifact=org.reactivestreams:reactive-streams:$REACTIVE_STREAM_VERSION:jar
$MVN_DOWNLOAD -Dartifact=mysql:mysql-connector-java:$MYSQL_VERSION:jar
$MVN_DOWNLOAD -Dartifact=javax.xml.bind:jaxb-api:$JAXB_VERSION:jar
$MVN_DOWNLOAD -Dartifact=org.glassfish.jaxb:jaxb-runtime:$JAXB_VERSION:jar
$MVN_DOWNLOAD -Dartifact=org.postgresql:postgresql:$POSTGRESQL_VERSION:jar

# Define the local Maven repository path
M2_REPOS="$HOME/.m2/repository"
JOOQ="$M2_REPOS/org/jooq"
MYSQL="$M2_REPOS/mysql/mysql-connector-java/$MYSQL_VERSION/mysql-connector-java-$MYSQL_VERSION.jar"
REACTIVE_STREAMS="$M2_REPOS/org/reactivestreams/reactive-streams/$REACTIVE_STREAM_VERSION/reactive-streams-$REACTIVE_STREAM_VERSION.jar"
JAXB_API="$M2_REPOS/javax/xml/bind/jaxb-api/$JAXB_VERSION/jaxb-api-$JAXB_VERSION.jar"
JAXB_RUNTIME="$M2_REPOS/org/glassfish/jaxb/jaxb-runtime/$JAXB_VERSION/jaxb-runtime-$JAXB_VERSION.jar"
POSTGRESQL_JAR="$M2_REPOS/org/postgresql/postgresql/$POSTGRESQL_VERSION/postgresql-$POSTGRESQL_VERSION.jar"

# Set the classpath (use ':' for Unix-like systems)
JARS="$JOOQ/jooq/$JOOQ_VERSION/jooq-$JOOQ_VERSION.jar:$JOOQ/jooq-meta/$JOOQ_VERSION/jooq-meta-$JOOQ_VERSION.jar:$JOOQ/jooq-codegen/$JOOQ_VERSION/jooq-codegen-$JOOQ_VERSION.jar:$REACTIVE_STREAMS:$MYSQL:$JAXB_API:$JAXB_RUNTIME:$POSTGRESQL_JAR"

# Run the jOOQ code generation tool
cd C:\killbill-moyasar-master
java -cp "$JARS" org.jooq.codegen.GenerationTool src/main/resources/gen.xml

# Pause the script execution to see the output
read -p "Press enter to continue..."

