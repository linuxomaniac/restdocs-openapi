package cc.dille.restdocs.openapi;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.BDDAssertions.thenThrownBy;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.Map;

import org.junit.Test;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.snippet.SnippetException;

public class RequestHandlerTest {

    private RequestHandler requestHandler = new RequestHandler();

    private Operation operation;

    private Map<String, Object> model;

    @Test
    public void should_add_request_information_to_model() {
        givenRequestWithJsonBody();

        whenModelGenerated();

        then(model).containsOnlyKeys("requestBodyFileName", "requestBodyPresent", "contentTypeRequest");
        then(model.get("requestBodyPresent")).isEqualTo(true);
        then(model.get("requestBodyFileName")).isEqualTo("test-request.json");
        then(model.get("contentTypeRequest")).isEqualTo(APPLICATION_JSON_VALUE);
    }

    @Test
    public void should_do_nothing_on_empty_body() {
        givenRequestWithoutBody();

        whenModelGenerated();

        then(model).isEmpty();
    }

    @Test
    public void should_fail_on_missing_field_documentation() {
        givenRequestWithJsonBody();

        thenThrownBy(() -> whenModelGeneratedWithFieldDescriptors(fieldWithPath("another").description("some")))
                .isInstanceOf(SnippetException.class)
                .hasMessageContaining("Fields with the following paths were not found in the payload: [another]")
                .hasMessageContaining("comment")
        ;
    }

    @Test
    public void should_add_schema_file() {
        givenRequestWithJsonBody();

        whenModelGeneratedWithFieldDescriptors(fieldWithPath("comment").description("some"));

        then(model).containsEntry("requestFieldsPresent", true);
        then(model).containsEntry("requestSchemaFileName", "test-schema-request.json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void should_default_content_type_to_application_json() {
        givenRequestWithJsonBodyWithoutContentType();

        whenModelGeneratedWithFieldDescriptors(fieldWithPath("comment").description("some"));

        then(model.get("contentTypeRequest")).isEqualTo(APPLICATION_JSON_VALUE);
    }

    private void whenModelGenerated() {
        model = requestHandler.generateModel(operation, OpenAPIResourceSnippetParameters.builder().build());
    }
    private void whenModelGeneratedWithFieldDescriptors(FieldDescriptor... fieldDescriptors) {
        model = requestHandler.generateModel(operation, OpenAPIResourceSnippetParameters.builder()
                .requestFields(fieldDescriptors)
                .build());
    }

    private void givenRequestWithJsonBody() {
        operation = new OperationBuilder()
                .request("http://localhost:8080/some/123")
                .method("POST")
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .content("{\"comment\": \"some\"}")
                .build();
    }

    private void givenRequestWithJsonBodyWithoutContentType() {
        operation = new OperationBuilder()
                .request("http://localhost:8080/some/123")
                .method("POST")
                .content("{\"comment\": \"some\"}")
                .build();
    }

    private void givenRequestWithoutBody() {
        operation = new OperationBuilder()
                .request("http://localhost:8080/some/123")
                .method("POST")
                .build();
    }
}