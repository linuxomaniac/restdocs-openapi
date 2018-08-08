package cc.dille.restdocs.openapi;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.IgnorableDescriptor;

import static cc.dille.restdocs.openapi.ParameterDescriptorWithOpenAPIType.OpenAPIScalarType.STRING;

/**
 * OpenAPI links have to have an operationId (and many other fields, see:
 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.1.md#linkObject).
 * The LinkDescriptor does not contain them, so we add a subclass that contains operationId for a starter.
 */
@RequiredArgsConstructor
@Getter
public class LinkDescriptorWithOpenAPIType extends IgnorableDescriptor<LinkDescriptorWithOpenAPIType> {

    private final String rel;

    private String operationId = null;

    public LinkDescriptorWithOpenAPIType operationId(String operationId) {
        this.operationId = operationId;
        return this;
    }

    protected static LinkDescriptorWithOpenAPIType fromLinkDescriptor(LinkDescriptor linkDescriptor) {
        LinkDescriptorWithOpenAPIType newDescriptor = new LinkDescriptorWithOpenAPIType(linkDescriptor.getRel());
        newDescriptor.description(linkDescriptor.getDescription());
        if(linkDescriptor.isIgnored()) {
            newDescriptor.ignored();
        }

        return newDescriptor;
    }
}
