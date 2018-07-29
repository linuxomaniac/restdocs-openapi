package cc.dille.restdocs.openapi;

import static cc.dille.restdocs.openapi.ParameterDescriptorWithOpenAPIType.OpenAPIScalarType.STRING;

import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.snippet.IgnorableDescriptor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * OpenAPI query and path parameters have to have a type and an example. The ParameterDescriptor does not contain them.
 * So we add a subclass that contains a type and an example.
 */
@RequiredArgsConstructor
@Getter
public class ParameterDescriptorWithOpenAPIType extends IgnorableDescriptor<ParameterDescriptorWithOpenAPIType> {

    private OpenAPIScalarType type = STRING;

    private final String name;

    private String example;

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

    protected static ParameterDescriptorWithOpenAPIType from(ParameterDescriptor parameterDescriptor) {
        ParameterDescriptorWithOpenAPIType newDescriptor = new ParameterDescriptorWithOpenAPIType(parameterDescriptor.getName());
        newDescriptor.description(parameterDescriptor.getDescription());
        if (parameterDescriptor.isOptional()) {
            newDescriptor.optional();
        }
        if (parameterDescriptor.isIgnored()) {
            newDescriptor.ignored();
        }
        newDescriptor.type(STRING);
        newDescriptor.example("some string");
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
