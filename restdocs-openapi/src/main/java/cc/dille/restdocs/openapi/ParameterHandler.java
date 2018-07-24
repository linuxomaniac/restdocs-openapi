package cc.dille.restdocs.openapi;

import com.sun.net.httpserver.HttpsParameters;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.Parameters;
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

    private final parameterType type;

    static ParameterHandler requestHeaderHandler() {
        return new ParameterHandler(parameterType.HEADER);
    }

    static ParameterHandler requestParameterHandler() {
        return new ParameterHandler(parameterType.REQUEST);
    }

    static ParameterHandler pathParameterHandler() {
        return new ParameterHandler(parameterType.PATH);
    }

    @Override
    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        Map<String, Object> model = new HashMap<>();

        switch (type) {
            case HEADER:
                List<HeaderDescriptor> headers = parameters.getRequestHeaders();

                if (!headers.isEmpty()) {
                    new RequestHeaderSnippetValidator(headers).validateHeaders(operation);

                    model.put("parametersPresent", true);
                    model.put("parameters", mapDescriptorsToModel(headers, operation.getRequest().getHeaders()));
                }
                break;

            case PATH:
                List<ParameterDescriptorWithOpenAPIType> pathParameters = parameters.getPathParameters();

                if (!pathParameters.isEmpty()) {
                    new PathParameterSnippetWrapper(pathParameters).validateParameters(operation);

                    model.put("parametersPresent", true);
                    model.put("parameters", mapDescriptorsToModel(pathParameters, operation.getRequest().getParameters()));
                }
                break;

            case REQUEST:
                List<ParameterDescriptorWithOpenAPIType> requestParameters = parameters.getRequestParameters();

                if (!requestParameters.isEmpty()) {
                    new RequestParameterSnippetWrapper(requestParameters).validateParameters(operation);

                    model.put("parametersPresent", true);
                    model.put("parameters", mapDescriptorsToModel(requestParameters, operation.getRequest().getParameters()));
                }
                break;
        }

        return model;
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<HeaderDescriptor> headerDescriptors, HttpHeaders presentHeaders) {
        return headerDescriptors.stream().map(headerDescriptor -> {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("name", headerDescriptor.getName());
            headerMap.put("description", (String) headerDescriptor.getDescription());
            headerMap.put("example", presentHeaders.getFirst(headerDescriptor.getName()));
            headerMap.put("in", type.getString());
            headerMap.put("type", "string");
            return headerMap;
        }).collect(toList());
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<ParameterDescriptorWithOpenAPIType> parametersDescriptors, Parameters presentParameters) {
        return parametersDescriptors.stream().map(parameterDescriptor -> {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("name", parameterDescriptor.getName());
            headerMap.put("description", (String) parameterDescriptor.getDescription());
            headerMap.put("example", presentParameters.getFirst(parameterDescriptor.getName()));
            headerMap.put("in", type.getString());
            headerMap.put("type", parameterDescriptor.getType().getTypeName());
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

    private enum parameterType {
        REQUEST("query"), PATH("path"), HEADER("header");

        private String string;

        public String getString() {
            return string;
        }

        private parameterType(String string) {
            this.string = string;
        }
    }
}
