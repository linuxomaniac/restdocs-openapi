# Restdocs-OpenAPI

This is the library providing the classes to deal with the generation of OpenAPI fragments with Restdocs.


## Working principle
_see `restdocs-openapi-example-project` for details_

After adding Restdocs and this library to your Spring project, write tests the
MockMVC way (see Restdocs documentation) using the classes provided by this
library.

Inside your tests, you document the expected results for each requests and
restdocs-openapi will generate fragments files documenting for each of them.
The library takes care of generating the schema and the example request.

Once all tests have been performed, the fragment files should be aggregated
in order to build the final OpenAPI YAML file. This is done through the Gradle
or Maven plugins, in wich a special task is triggered after the tests.


## Adding restdocs-openapi to your project

_TODO, see `restdocs-openapi-example-project`_
