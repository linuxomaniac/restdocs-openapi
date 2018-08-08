package cc.dille.restdocs.openapi.example.doc;

import cc.dille.restdocs.openapi.OpenAPIResourceSnippetParameters;
import org.junit.Test;

import static cc.dille.restdocs.openapi.OpenAPIResourceDocumentation.openAPIResource;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class IndexTest extends RestDocTest {

    @Test
    public void Test00_listing() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk())
        .andDo(document("index-listing",
                openAPIResource(OpenAPIResourceSnippetParameters.builder()
                        .description("Get the links of the api")
                        .responseFields(
                                fieldWithPath("_links").description("Links to other resources"))
                        .links(
                                linkWithRel("self").description("This self reference"),
                                linkWithRel("note").description("The link to the greetings"))
                        .build())));
    }
}
