package cc.dille.restdocs.openapi.example.doc;

import cc.dille.restdocs.openapi.OpenAPIResourceSnippetParameters;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.hateoas.MediaTypes;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;

import static cc.dille.restdocs.openapi.OpenAPIResourceDocumentation.openAPIResource;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Some tests depend of each others, so we need to call them in order
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoteTest extends RestDocTest {
    private FieldDescriptor[] noteFields = new FieldDescriptor[]{
            fieldWithPath("id").description("The id of the Note").type(JsonFieldType.NUMBER),
            fieldWithPath("content").description("The content of the Note").type(JsonFieldType.STRING)};

    @Test
    public void Test00_post() throws Exception {
        mockMvc.perform(post("/note").contentType(MediaTypes.HAL_JSON_VALUE)
                .content(objectMapper.writeValueAsString(TestValues.NOTE_0)))
                .andExpect(status().isCreated())
                .andDo(document("note-post",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .operationId("postNote")
                                .summary("Creates a note")
                                // It's a bit confusing, but description is for the current status code,
                                // whereas summary is for the whole method.
                                // description is more like the behavior / the expected result of the current request.
                                .description("New note is Created")
                                .requestFields(noteFields)
                                .build())));
    }

    @Test
    public void Test01_listing() throws Exception {
        mockMvc.perform(get("/note").accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(TestValues.NOTE_0.getId()))
                .andExpect(jsonPath("$[0].content").value(TestValues.NOTE_0.getContent()))
                .andDo(document("note-listing",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .operationId("getNotes")
                                .summary("Gets all the recorded notes")
                                .description("Returns notes")
                                .responseFields(fieldWithPath("[]").description("Array of notes"),
                                        fieldWithPath("[].id").description("The id").type(JsonFieldType.NUMBER),
                                        fieldWithPath("[].content").description("the content").type(JsonFieldType.STRING))
                                .build())));
    }

    @Test
    public void Test02_getOk() throws Exception {
        mockMvc.perform(get("/note/{id}", TestValues.NOTE_0.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TestValues.NOTE_0.getId()))
                .andExpect(jsonPath("$.content").value(TestValues.NOTE_0.getContent()))
                .andDo(document("note-get-ok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .operationId("getNote")
                                .summary("Gets a Note")
                                .description("Returns the requested note")
                                .pathParameters(parameterWithName("id").description("The id of the Note to get"))
                                .build())));
    }

    @Test
    public void Test03_getNok() throws Exception {
        mockMvc.perform(get("/note/{id}", TestValues.NOTE_1.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andDo(document("note-get-nok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .operationId("getNote")
                                .summary("Gets a note")
                                .description("No Note matching requested id found")
                                .pathParameters(parameterWithName("id").description("The id of the Note to get"))
                                .build())));
    }

    @Test
    public void Test04_deleteOk() throws Exception {
        mockMvc.perform(delete("/note/{id}", TestValues.NOTE_0.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNoContent())
                .andDo(document("note-delete-ok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .operationId("deleteNote")
                                .summary("Deletes a note")
                                .description("The note has been deleted")
                                .pathParameters(parameterWithName("id").description("The id of the Note to delete"))
                                .build())));
    }

    @Test
    public void Test05_deleteNok() throws Exception {
        mockMvc.perform(delete("/note/{id}", TestValues.NOTE_1.getId()).accept(MediaTypes.HAL_JSON_VALUE))
                .andExpect(status().isNotFound())
                .andDo(document("note-delete-nok",
                        openAPIResource(OpenAPIResourceSnippetParameters.builder()
                                .operationId("deleteNote")
                                .summary("Deletes a note")
                                .description("Deletion failed no such Note with requested id")
                                .pathParameters(parameterWithName("id").description("The id of the Note to delete"))
                                .build())));
    }
}
