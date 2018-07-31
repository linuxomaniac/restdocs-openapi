package cc.dille.restdocs.openapi;

import static cc.dille.restdocs.openapi.ParameterDescriptorWithOpenAPIType.OpenAPIScalarType.STRING;

import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.IgnorableDescriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.management.Descriptor;

/**
 * OpenAPI parameters have to have a type and an example. The ParameterDescriptor does not contain them.
 * So we add a subclass that contains type and example.
 */
@RequiredArgsConstructor
@Getter
public class ParameterDescriptorWithOpenAPIType extends IgnorableDescriptor<ParameterDescriptorWithOpenAPIType> {

    private OpenAPIScalarType type = STRING;

    private final String name;

    private String example = null;

    private String in = null;

    private boolean optional;

    public ParameterDescriptorWithOpenAPIType type(OpenAPIScalarType type) {
        this.type = type;
        return this;
    }

    public ParameterDescriptorWithOpenAPIType optional() {
        this.optional = true;
        return this;
    }

    public ParameterDescriptorWithOpenAPIType example(String example) {
        this.example = example;
        return this;
    }

    public ParameterDescriptorWithOpenAPIType in(String in) {
        this.in = in;
        return this;
    }

    protected static ParameterDescriptorWithOpenAPIType fromRequestParameter(ParameterDescriptor parameterDescriptor) {
        return from(parameterDescriptor.getName(), (String) parameterDescriptor.getDescription(), "query", parameterDescriptor.isOptional(), parameterDescriptor.isIgnored());
    }

    protected static ParameterDescriptorWithOpenAPIType fromPathParameter(ParameterDescriptor parameterDescriptor) {
        return from(parameterDescriptor.getName(), (String) parameterDescriptor.getDescription(), "path", parameterDescriptor.isOptional(), parameterDescriptor.isIgnored());
    }

    protected static ParameterDescriptorWithOpenAPIType fromRequestHeader(HeaderDescriptor headerDescriptor) {
        return from(headerDescriptor.getName(), (String) headerDescriptor.getDescription(), "header", headerDescriptor.isOptional(), false);
    }

    private static ParameterDescriptorWithOpenAPIType from(String name, String description, String in, boolean optional, boolean ignored) {
        ParameterDescriptorWithOpenAPIType newDescriptor = new ParameterDescriptorWithOpenAPIType(name);
        newDescriptor.description(description);
        if (optional) {
            newDescriptor.optional();
        }
        if (ignored) {
            newDescriptor.ignored();
        }
        newDescriptor.in(in);
        newDescriptor.type(STRING);

        return newDescriptor;
    }

    public enum OpenAPIScalarType {
        NUMBER("number"),
        INTEGER("integer"),
        STRING("string"),
        BOOLEAN("boolean"),
        TIME_ONLY("time-only"),
        DATE_ONLY("date-only"),
        DATETIME_ONLY("datetime-only"),
        DATETIME("datetime");

        @Getter
        private String typeName;

        OpenAPIScalarType(String typeName) {
            this.typeName = typeName;
        }
    }
}
