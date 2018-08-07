package cc.dille.hello.doc;

import cc.dille.restdocs.openapi.OpenAPIResourceSnippetParameters;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.payload.FieldDescriptor;

import static cc.dille.restdocs.openapi.OpenAPIResourceDocumentation.openAPIResource;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.restdocs.payload.JsonFieldType;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GreetingTest extends RestDocTest {
    private FieldDescriptor[] greetingFields = new FieldDescriptor[]{
            fieldWithPath("id").description("The id of the Greeting").type(JsonFieldType.NUMBER),
            fieldWithPath("content").description("The content of the Greeting").type(JsonFieldType.STRING)};

    @Test
    public void Test00_post() throws Exception {
        mockMvc.perform(post("/greeting").contentType(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(TestValues.greeting0)))
                .andExpect(status().isCreated())
                .andDo(document("greeting-post",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .description("Creates a Greeting")
                                .requestFields(greetingFields)
                                .build())));
    }

    @Test
    public void Test01_listing() throws Exception {
        mockMvc.perform(get("/greeting").accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TestValues.greeting0.getId()))
                .andExpect(jsonPath("$[0].content").value(TestValues.greeting0.getContent()))
                .andDo(document("greeting-listing",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .description("Gets all the recorded Greetings")
                                .responseFields(fieldWithPath("[]").description("Array of Greetings"),
                                        fieldWithPath("[].id").description("The id").type(JsonFieldType.NUMBER),
                                        fieldWithPath("[].content").description("the content").type(JsonFieldType.STRING))
                                .build())));
    }

    @Test
    public void Test02_getOk() throws Exception {
        mockMvc.perform(get("/greeting/{id}", TestValues.greeting0.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestValues.greeting0.getId()))
                .andExpect(jsonPath("$.content").value(TestValues.greeting0.getContent()))
                .andDo(document("greeting-get-ok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .description("Gets a Greeting")
                                .pathParameters(parameterWithName("id").description("The id of the Greeting to get"))
                                .build())));
    }

    @Test
    public void Test03_getNok() throws Exception {
        mockMvc.perform(get("/greeting/{id}", TestValues.greeting1.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andDo(document("greeting-get-nok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .description("No Greeting matching requested id found")
                                .pathParameters(parameterWithName("id").description("The id of the Greeting to get"))
                                .build())));
    }

    @Test
    public void Test04_deleteOk() throws Exception {
        mockMvc.perform(delete("/greeting/{id}", TestValues.greeting0.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andDo(document("greeting-delete-ok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .description("Deletes a Greeting")
                                .pathParameters(parameterWithName("id").description("The id of the Greeting to delete"))
                                .build())));
    }

    @Test
    public void Test05_deleteNok() throws Exception {
        mockMvc.perform(delete("/greeting/{id}", TestValues.greeting1.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andDo(document("greeting-delete-nok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .description("Deletion failed no such Greeting with requested id")
                                .pathParameters(parameterWithName("id").description("The id of the Greeting to delete"))
                                .build())));
    }
}
