package cc.dille.restdocs.openapi;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.BDDAssertions.then;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.links;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.test.context.junit4.SpringRunner;

import lombok.SneakyThrows;

@RunWith(SpringRunner.class)
@WebMvcTest
public class OpenAPIDocumentationIntegrationTest extends OpenAPIResourceSnippetIntegrationTest implements RestResourceSnippetTestTrait {

    @Test
    @SneakyThrows
    public void should_document_both_restdocs_and_openAPI() {
        givenEndpointInvoked();

        whenDocumentedWithRestdocsAndOpenAPI();

        thenRestdocsAndOpenAPIFilesExist();
    }

    @Test
    @SneakyThrows
    public void should_document_both_restdocs_and_openAPI_as_private_resource() {
        givenEndpointInvoked();

        whenDocumentedAsPrivateResource();

        thenRestdocsAndOpenAPIFilesExist();
    }

    @Test
    @SneakyThrows
    public void should_document_using_the_passed_openAPI_snippet() {
        givenEndpointInvoked();

        whenDocumentedWithOpenAPISnippet();

        thenRestdocsAndOpenAPIFilesExist();
    }

    @Test
    @SneakyThrows
    public void should_value_ignored_fields_and_links() {
        givenEndpointInvoked();

        assertThatCode(
            this::whenDocumentedWithAllFieldsLinksIgnored
        ).doesNotThrowAnyException();
    }

    private void whenDocumentedWithRestdocsAndOpenAPI() throws Exception {
        resultActions
                .andDo(print())
                .andDo(
                        OpenAPIDocumentation.document(operationName,
                                pathParameters(
                                        parameterWithName("someId").description("someId"),
                                        parameterWithName("otherId").description("otherId")
                                ),
                                requestFields(fieldDescriptors().getFieldDescriptors()),
                                requestHeaders(
                                        headerWithName("X-Custom-Header").description("some custom header")
                                ),
                                responseFields(
                                        fieldWithPath("comment").description("the comment"),
                                        fieldWithPath("flag").description("the flag"),
                                        fieldWithPath("count").description("the count"),
                                        fieldWithPath("id").description("id"),
                                        subsectionWithPath("_links").ignored()
                                ),
                                responseHeaders(
                                        headerWithName("X-Custom-Header").description("some custom header")
                                ),
                                links(
                                        linkWithRel("self").description("some"),
                                        linkWithRel("multiple").description("multiple")
                                )
                        )
                );
    }

    private void whenDocumentedWithOpenAPISnippet() throws Exception {
        resultActions
            .andDo(
                OpenAPIDocumentation.document(operationName,
                                           buildFullOpenAPIResourceSnippet())
            );
    }

    private void whenDocumentedWithAllFieldsLinksIgnored() throws Exception {
        resultActions
            .andDo(
                OpenAPIDocumentation.document(operationName,
                    requestFields(fieldDescriptors().getFieldDescriptors()),
                    responseFields(
                        fieldWithPath("comment").ignored(),
                        fieldWithPath("flag").ignored(),
                        fieldWithPath("count").ignored(),
                        fieldWithPath("id").ignored(),
                        subsectionWithPath("_links").ignored()
                    ),
                    links(
                            linkWithRel("self").optional().ignored(),
                            linkWithRel("multiple").optional().ignored()
                    )
                )
            );
    }

    private void whenDocumentedAsPrivateResource() throws Exception {
        OperationRequestPreprocessor operationRequestPreprocessor = r -> r;
        resultActions
                .andDo(
                        OpenAPIDocumentation.document(operationName,
                                true,
                                operationRequestPreprocessor,
                                requestFields(fieldDescriptors().getFieldDescriptors()),
                                responseFields(
                                        fieldWithPath("comment").description("the comment"),
                                        fieldWithPath("flag").description("the flag"),
                                        fieldWithPath("count").description("the count"),
                                        fieldWithPath("id").description("id"),
                                        subsectionWithPath("_links").ignored()
                                ),
                                links(
                                        linkWithRel("self").description("some"),
                                        linkWithRel("multiple").description("multiple")
                                )
                        )
                );
    }

    private void thenRestdocsAndOpenAPIFilesExist() {
        then(generatedOpenAPIFragmentFile()).exists();
        then(generatedRequestJsonFile()).exists();
        then(generatedResponseJsonFile()).exists();
        then(generatedRequestSchemaFile()).exists();
        then(generatedResponseSchemaFile()).exists();

        then(generatedCurlAdocFile()).exists();
        then(generatedHttpRequestAdocFile()).exists();
        then(generatedHttpResponseAdocFile()).exists();
    }

}
