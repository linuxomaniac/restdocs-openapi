package cc.dille.restdocs.openapi;

import static cc.dille.restdocs.openapi.ParameterDescriptorWithOpenAPIType.OpenAPIScalarType.INTEGER;
import static cc.dille.restdocs.openapi.OpenAPIResourceDocumentation.parameterWithName;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.restdocs.generate.RestDocumentationGenerator.ATTRIBUTE_NAME_URL_TEMPLATE;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.SnippetException;

public class PathParameterHandlerTest {

    private ParameterHandler pathParameterHandler = new ParameterHandler();

    private Operation operation;

    private Map<String, Object> model;

    @SuppressWarnings("unchecked")
    @Test
    public void should_add_path_parameters_to_model() {
        givenRequestWithoutBody();

        whenGenerateInvokedWithPathParameters();

        then(model).containsEntry("parametersPresent", true);
        then(model).containsKey("parameters");
        then(model.get("parameters")).isInstanceOf(List.class);
        List<Map<String, Object>> pathParameters = (List<Map<String, Object>>) model.get("parameters");
        then(pathParameters).hasSize(2);
        then(pathParameters.get(0)).containsEntry("name", "id");
        then(pathParameters.get(0)).containsEntry("in", "path");
        then(pathParameters.get(0)).containsEntry("type", "string");
        then(pathParameters.get(0)).containsEntry("description", "an id");
        then(pathParameters.get(0)).containsEntry("required", "true");
        then(pathParameters.get(0)).containsEntry("example", "you lost");

        then(pathParameters.get(1)).containsEntry("name", "other");
        then(pathParameters.get(1)).containsEntry("in", "path");
        then(pathParameters.get(1)).containsEntry("type", "integer");
        then(pathParameters.get(1)).containsEntry("description", "other");
        then(pathParameters.get(1)).containsEntry("required", "true");
        then(pathParameters.get(1)).containsEntry("example", "42");
    }

    @Test
    public void should_do_nothing_if_no_path_parameters_documented() {
        givenRequestWithoutBody();

        whenGenerateInvokedWithoutPathParameters();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_invalid_path_parameter_documentation() {
        givenRequestWithoutBody();

        thenThrownBy(this::whenGenerateInvokedWithInvalidPathParameters).isInstanceOf(SnippetException.class);
    }

    private void whenGenerateInvokedWithPathParameters() {
        model = pathParameterHandler.generateModel(operation, OpenAPIResourceSnippetParameters.builder()
                .pathParameters(
                        parameterWithName("id").description("an id").example("you lost"),
                        parameterWithName("other").type(INTEGER).description("other").example("42")
                ).build());
    }

    private void whenGenerateInvokedWithInvalidPathParameters() {
        model = pathParameterHandler.generateModel(operation, OpenAPIResourceSnippetParameters.builder()
                .pathParameters(
                        parameterWithName("id").description("an id"),
                        parameterWithName("other-x").type(INTEGER).description("other")
                ).build());
    }

    private void whenGenerateInvokedWithoutPathParameters() {
        model = pathParameterHandler.generateModel(operation, OpenAPIResourceSnippetParameters.builder().build());
    }

    private void givenRequestWithoutBody() {
        operation = new OperationBuilder()
                .attribute(ATTRIBUTE_NAME_URL_TEMPLATE, "http://localhost:8080/some/{id}/other/{other}")
                .request("http://localhost:8080/some/12/other/34")
                .method("POST")
                .build();
    }
}