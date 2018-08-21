# Restdocs-OpenAPI

Highly inspired by https://github.com/ePages-de/restdocs-raml
An attempt to generate OpenAPI documentation with Spring Restdocs.


## Project structure

This project consists of several subprojects, each one contains a `README.md` file:

- `restdocs-openapi` is the lib to use with Sping Restodcs and that generates fragment files
- `restdocs-openapi-plugin` is the plugin for Gradle and Maven to aggregate fragment files
- `restdocs-openapi-example-project` is a dummy project that shows the usage of the lib and the plugins for both Gradle and Maven
- `swagger-ui-test` is a nodeJS standalone web UI for visualising the generated OpenAPI files


## Useful links
- OpenAPI 3.0.1 dpecification: https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md
- Introduction to Spring Restdocs: https://docs.spring.io/spring-restdocs/docs/current/reference/html5/
