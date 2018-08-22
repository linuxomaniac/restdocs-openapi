# Restdocs-openapi-plugin

Gradle and Maven plugins to aggregate fragments generated during the tests
with the `restdocs-openapi` library.

These plugins are called after the tests are passed to generate the final
OpenAPI file.

## Compilation

Gradle and Maven plugins share common code, so that's the purpose of
`restdocs-openapi-plugin-common`. The way used for the Gradle and Maven plugin
to load this code is trough the local Maven repository. Since the two plugins
are built using Gradle, the goal `publishToMavenLocal` is used to
automatically build the jars and add them to the local repo.

### Requirements

If you want to build the Maven plugin, you will need both Gradle and Maven,
otherwise only Gradle is needed.

### The easy way

Just go in this directory (`restdocs-openapi-plugin`) and run
`./gradlew publishToMavenLocal` to compile the two plugins and the common
library.

### Building the common library

Go in the subdirectory `plugin-common` and run `gradle publishToMavenLocal`
(or `gradle jar` if you only need the jar).

### Building for Gradle and Maven

Be sure to have built `plugin-common` and that the generated jar is in the
classpath (or in the local Maven repository).
Go into the subdirectory (`plugin-gradle` or `plugin-maven`) and run
`gradle publishToMavenLocal`.


## Testing

Test classes are provided for testing the behavior of the plugins. The basics
plugin functions are testing in the `plugin-common` library, whereas all the
integrations tests are located in each plugin directory.

For the Gradle and Maven plugin, be sure to have built the common library
(and published it) before running the tests.

To run the tests, simply go into the wanted subdirectory and run `gradle test`.

The special exception is for the Maven plugin: because it is built with Gradle,
there are some tricks to know. In fact Gradle is invoking Maven to generate some
required files, so does the integration tests. Before running the tests, you
have to provide the Maven home in the environment variables so that Maven can be
launched for the integration tests.
The procedure is quite simple, to know the home directory, simply run `mvn -v`
from command line. It should produce something like:
```text
  Apache Maven 3.3.9 (xxx; 2015-11-10T17:41:47+01:00)
  Maven home: /opt/apache-maven
  Java version: 1.8.0_182, vendor: Oracle Corporation
  Java home: /usr/lib/jvm/openjdk-1.8.0_182/jre
  Default locale: en_US, platform encoding: UTF-8
  OS name: "linux", version: "4.17.16_1", arch: "amd64", family: "unix
```
The Maven home directory is then `/opt/apache-maven` in this example. To fill
the environment variable and run the tests you then will have to launch
`M2_HOME=/opt/apache-maven gradle test`, and it will do!


## Using the plugin

Once the plugin has been added to Gradle or Maven, a new task is available:
`openapidoc` for Gradle and `restdocs-openapi:openapidoc` for Maven.
You can automatically trigger these tasks when the tests are done, or you can
manually run them; example with Gradle: `./gradlew clean test openapidoc`.
The generated YAML file will then be found under `build/openAPIDoc/api.yaml`.