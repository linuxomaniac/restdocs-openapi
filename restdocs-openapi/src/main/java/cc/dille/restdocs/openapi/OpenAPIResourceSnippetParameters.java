package cc.dille.restdocs.openapi;

import static java.util.Collections.emptyList;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.Attributes.Attribute;
import org.springframework.util.ReflectionUtils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@AllArgsConstructor(access = PRIVATE)
@Getter
public class OpenAPIResourceSnippetParameters {

    // statusDescription is the documentation of current status of the reponse
    private final String statusDescription;
    // summary is the documentation of the method
    private final String summary;
    private final String operationId;
    private boolean privateResource;
    private final List<FieldDescriptor> requestFields;
    private final List<FieldDescriptor> responseFields;
    private final List<LinkDescriptorWithOpenAPIType> links;
    private final List<ParameterDescriptorWithOpenAPIType> requestParameters;
    private final List<HeaderDescriptor> responseHeaders;

    List<FieldDescriptor> getResponseFieldsWithLinks() {
        List<FieldDescriptor> combinedDescriptors = new ArrayList<>(getResponseFields());
        combinedDescriptors.addAll(
                getLinks().stream()
                        .map(OpenAPIResourceSnippetParameters::toFieldDescriptor)
                        .collect(Collectors.toList())
        );
        return combinedDescriptors;
    }

    private static FieldDescriptor toFieldDescriptor(LinkDescriptorWithOpenAPIType linkDescriptor) {

        FieldDescriptor descriptor = createLinkFieldDescriptor(linkDescriptor.getRel())
                .description(linkDescriptor.getDescription())
                .type(JsonFieldType.VARIES)
                .attributes(linkDescriptor.getAttributes().entrySet().stream()
                        .map(e -> new Attribute(e.getKey(), e.getValue()))
                        .toArray(Attribute[]::new));

        if (linkDescriptor.isIgnored()) {
            descriptor.ignored();
        }

        return descriptor;
    }

    /**
     * Behaviour changed from restdocs 1.1 to restdocs 1.2
     * In 1.2 you need to document attributes inside the object when documenting the object with fieldWithPath - which was not the case with 1.1
     * So we need to use subsectionWithPath if we are working with 1.2 and fieldWithPath otherwise
     * @param rel
     * @return
     */
    private static FieldDescriptor createLinkFieldDescriptor(String rel) {
        String path = "_links." + rel;
        return (FieldDescriptor) Optional.ofNullable(ReflectionUtils.findMethod(PayloadDocumentation.class, "subsectionWithPath", String.class))
                .map(m -> ReflectionUtils.invokeMethod(m, null, path))
                .orElseGet(() -> fieldWithPath(path));
    }

    public static class OpenAPIResourceSnippetParametersBuilder {

        private List<FieldDescriptor> requestFields = new ArrayList<>();
        private List<FieldDescriptor> responseFields = new ArrayList<>();
        private List<LinkDescriptorWithOpenAPIType> links = new ArrayList<>();
        private List<ParameterDescriptorWithOpenAPIType> requestParameters = new ArrayList<>();
        private List<HeaderDescriptor> responseHeaders = new ArrayList<>();

        public OpenAPIResourceSnippetParametersBuilder requestFields(FieldDescriptor... requestFields) {
            this.requestFields.addAll(Arrays.asList(requestFields));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder requestFields(FieldDescriptors requestFields) {
            this.requestFields.addAll(requestFields.getFieldDescriptors());
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder responseFields(FieldDescriptor... responseFields) {
            this.responseFields.addAll(Arrays.asList(responseFields));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder responseFields(FieldDescriptors responseFields) {
            this.responseFields.addAll(responseFields.getFieldDescriptors());
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder links(LinkDescriptorWithOpenAPIType... links) {
            this.links.addAll(Arrays.asList(links));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder links(LinkDescriptor... links) {
            this.links.addAll(Stream.of(links).map(LinkDescriptorWithOpenAPIType::fromLinkDescriptor).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder pathParameters(ParameterDescriptor... pathParameters) {
            this.requestParameters.addAll(Stream.of(pathParameters).map(ParameterDescriptorWithOpenAPIType::fromPathParameter).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder pathParameters(ParameterDescriptorWithOpenAPIType... pathParameters) {
            this.requestParameters.addAll(Stream.of(pathParameters).map(p -> p.in("path")).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder requestParameters(ParameterDescriptor... requestParameters) {
            this.requestParameters.addAll(Stream.of(requestParameters).map(ParameterDescriptorWithOpenAPIType::fromRequestParameter).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder requestParameters(ParameterDescriptorWithOpenAPIType... requestParameters) {
            this.requestParameters.addAll(Stream.of(requestParameters).map(p -> p.in("query")).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder requestHeaders(HeaderDescriptor... requestHeaders) {
            this.requestParameters.addAll(Stream.of(requestHeaders).map(ParameterDescriptorWithOpenAPIType::fromRequestHeader).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder requestHeaders(ParameterDescriptorWithOpenAPIType... requestHeaders) {
            this.requestParameters.addAll(Stream.of(requestHeaders).map(p -> p.in("header")).collect(Collectors.toList()));
            return this;
        }

        public OpenAPIResourceSnippetParametersBuilder responseHeaders(HeaderDescriptor... responseHeaders) {
            this.responseHeaders.addAll(Arrays.asList(responseHeaders));
            return this;
        }
    }
}
