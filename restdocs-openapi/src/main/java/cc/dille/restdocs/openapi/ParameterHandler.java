package cc.dille.restdocs.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

@RequiredArgsConstructor
class ParameterHandler implements OperationHandler {

    private final String modelNamePrefix;
    private final Function<List<HeaderDescriptor>, HeadersValidator> validatorSupplier;
    private final Function<OpenAPIResourceSnippetParameters, List<HeaderDescriptor>> descriptorSupplier;
    private final Function<Operation, HttpHeaders> headersSupplier;

    static ParameterHandler requestHeaderHandler() {
        return new ParameterHandler("requestHeader",
                RequestHeaderSnippetValidator::new,
                OpenAPIResourceSnippetParameters::getRequestHeaders,
                o -> o.getRequest().getHeaders());
    }

    static ParameterHandler queryParameterHandler() {
        return new ParameterHandler("requestParameter",
                RequestParameterSnippetWrapper::new,
                OpenAPIResourceSnippetParameters::getRequestParameters,
                o -> o.getRequest().getParameters());
    }

    static ParameterHandler pathParameterHandler() {
        return new ParameterHandler("pathParameter",
                PathParameterSnippetWrapper::new,
                OpenAPIResourceSnippetParameters::getPathParameters,
                o -> o.);
    }

    @Override
    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        List<HeaderDescriptor> headers = descriptorSupplier.apply(parameters);
        if (!headers.isEmpty()) {
            validatorSupplier.apply(headers).validateHeaders(operation);
            Map<String, Object> model = new HashMap<>();
            model.put("parametersPresent", true);
            model.put("parameters", mapDescriptorsToModel(headers, headersSupplier.apply(operation)));
            return model;
        }
        return emptyMap();
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<HeaderDescriptor> headerDescriptors, HttpHeaders presentHeaders) {
        return headerDescriptors.stream().map(headerDescriptor -> {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("name", headerDescriptor.getName());
            headerMap.put("description", (String) headerDescriptor.getDescription());
            headerMap.put("example", presentHeaders.getFirst(headerDescriptor.getName()));
            return headerMap;
        }).collect(toList());
    }

    private interface HeadersValidator {
        void validateHeaders(Operation operation);
    }

    private interface ParametersValidator {
        void validateParameters(Operation operation);
    }

    private static class RequestHeaderSnippetValidator extends RequestHeadersSnippet implements HeadersValidator {
        private RequestHeaderSnippetValidator(List<HeaderDescriptor> descriptors) {
            super(descriptors);
        }

        @Override
        public void validateHeaders(Operation operation) {
            super.createModel(operation);
        }
    }

    static class RequestParameterSnippetWrapper extends RequestParametersSnippet implements ParametersValidator {

        RequestParameterSnippetWrapper(List<ParameterDescriptorWithOpenAPIType> descriptors) {
            super(descriptors.stream().map(d -> parameterWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
        }

        @Override
        public void validateParameters(Operation operation) {
            super.createModel(operation);
        }
    }

    static class PathParameterSnippetWrapper extends PathParametersSnippet implements ParametersValidator {

        PathParameterSnippetWrapper(List<ParameterDescriptorWithOpenAPIType> descriptors) {
            super(descriptors.stream().map(d -> parameterWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
        }

        @Override
        public void validateParameters(Operation operation) {
            super.createModel(operation);
        }
    }
}
