package cc.dille.restdocs.openapi;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.snippet.SnippetException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;

public class HeaderHandlerTest {

    private Operation operation;
    private OpenAPIResourceSnippetParameters snippetParameters;
    private Map<String, Object> model;

    @Test
    public void should_add_request_model_header() {
        givenRequestWithHeaders();
        givenDocumentedRequestHeaders();

        whenRequestModelGenerated();

        then(model).containsEntry("parametersPresent", true);
        then(model).containsKey("parameters");
        thenHeadersModelContainsTuples("parameters",
                tuple(AUTHORIZATION, "Authorization header", "Basic some", "string", "header", "true"),
                tuple(ACCEPT, "Accept", HAL_JSON_VALUE, "string", "header", "false")
        );
    }

    @Test
    public void should_add_response_model_header() {
        givenRequestWithHeaders();
        givenDocumentedResponseHeaders();

        whenResponseModelGenerated();

        then(model).containsEntry("responseHeadersPresent", true);
        then(model).containsKey("responseHeaders");
        thenHeadersModelContainsTuples("responseHeaders",
                tuple(CONTENT_TYPE, "ContentType", HAL_JSON_VALUE, null, null, null)
        );
    }

    @Test
    public void should_do_nothing_if_no_headers_documented() {
        givenRequestWithoutHeaders();
        givenNoHeadersDocumented();

        whenResponseModelGenerated();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_missing_documented_header() {
        givenRequestWithoutHeaders();
        givenDocumentedResponseHeaders();

        thenThrownBy(this::whenResponseModelGenerated).isInstanceOf(SnippetException.class);
    }

    @SuppressWarnings("unchecked")
    private void thenHeadersModelContainsTuples(String headersModelAttributeName, Tuple... expectedTuples) {
        then((List<Map<String, Object>>) model.get(headersModelAttributeName))
                .extracting(
                        m -> m.get("name"),
                        m -> m.get("description"),
                        m -> m.get("example"),
                        m -> m.get("type"),
                        m -> m.get("in"),
                        m -> m.get("required")
                )
                .containsOnly(expectedTuples);
    }

    private void whenRequestModelGenerated() {
        model = new ParameterHandler().generateModel(operation, snippetParameters);
    }

    private void whenResponseModelGenerated() {
        model = new ResponseHeaderHandler().generateModel(operation, snippetParameters);
    }

    private void givenDocumentedRequestHeaders() {
        snippetParameters = OpenAPIResourceSnippetParameters.builder()
                .requestHeaders(
                        headerWithName(AUTHORIZATION).description("Authorization header"),
                        headerWithName(ACCEPT).description("Accept").optional()
                )
                .build();
    }

    private void givenDocumentedResponseHeaders() {
        snippetParameters = OpenAPIResourceSnippetParameters.builder()
                .responseHeaders(
                        headerWithName(CONTENT_TYPE).description("ContentType")
                )
                .build();
    }

    private void givenNoHeadersDocumented() {
        snippetParameters = OpenAPIResourceSnippetParameters.builder().build();
    }

    private void givenRequestWithHeaders() {
        OperationBuilder operationBuilder = new OperationBuilder();

        operationBuilder
                .request("http://localhost:8080/some")
                .header(AUTHORIZATION, "Basic some")
                .header(ACCEPT, HAL_JSON_VALUE)
                .method("POST");

        operationBuilder.response()
                .header(CONTENT_TYPE, HAL_JSON_VALUE);

        operation = operationBuilder.build();
    }

    private void givenRequestWithoutHeaders() {
        OperationBuilder operationBuilder = new OperationBuilder();

        operation = operationBuilder
                .request("http://localhost:8080/some")
                .build();
    }
}
