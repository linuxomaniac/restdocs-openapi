package cc.dille.restdocs.openapi;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;
import org.springframework.restdocs.request.RequestDocumentation;

import cc.dille.restdocs.openapi.ParameterDescriptorWithOpenAPIType.OpenAPIScalarType;

public class ParameterDescriptorWithOpenAPITypeTest {

    private ParameterDescriptorWithOpenAPIType descriptor;

    @Test
    public void should_convert_restdocs_parameter_descriptor() {
        whenParameterDescriptorCreatedFromRestDocsParameter();

        then(descriptor.isOptional()).isTrue();
        then(descriptor.getDescription()).isNotNull();
        then(descriptor.getType()).isEqualTo(OpenAPIScalarType.STRING);
    }

    private void whenParameterDescriptorCreatedFromRestDocsParameter() {
        descriptor = ParameterDescriptorWithOpenAPIType.from(RequestDocumentation.parameterWithName("some")
                .description("some")
                .optional());
    }
}