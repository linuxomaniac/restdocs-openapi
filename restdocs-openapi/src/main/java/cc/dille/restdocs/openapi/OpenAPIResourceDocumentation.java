package cc.dille.restdocs.openapi;

import org.springframework.restdocs.payload.FieldDescriptor;

public abstract class OpenAPIResourceDocumentation {

    public static OpenAPIResourceSnippet openAPIResource(OpenAPIResourceSnippetParameters openAPIResourceSnippetParameters) {
        return new OpenAPIResourceSnippet(openAPIResourceSnippetParameters);
    }

    public static OpenAPIResourceSnippet openAPIResource() {
        return new OpenAPIResourceSnippet(OpenAPIResourceSnippetParameters.builder().build());
    }

    public static OpenAPIResourceSnippet openAPIResource(String description) {
        return new OpenAPIResourceSnippet(OpenAPIResourceSnippetParameters.builder().description(description).build());
    }

    public static FieldDescriptors fields(FieldDescriptor... fieldDescriptors) {
        return new FieldDescriptors(fieldDescriptors);
    }

    public static ParameterDescriptorWithOpenAPIType parameterWithName(String name) {
        return new ParameterDescriptorWithOpenAPIType(name);
    }

    public static LinkDescriptorWithOpenAPIType linkWithRel(String rel) {
        return new LinkDescriptorWithOpenAPIType(rel);
    }
}
