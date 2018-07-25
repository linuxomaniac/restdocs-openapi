package cc.dille.restdocs.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.Parameters;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

@RequiredArgsConstructor
class ParameterHandler implements OperationHandler {

    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        List<Map<String, String>> res = new ArrayList<>();

        List<HeaderDescriptor> headers = parameters.getRequestHeaders();
        if (!headers.isEmpty()) {
            new RequestHeaderSnippetValidator(headers).validate(operation);
            res.addAll(mapDescriptorsToModel(headers, operation.getRequest().getHeaders()) );
        }

        List<ParameterDescriptorWithOpenAPIType> pathParameters = parameters.getPathParameters();
        if (!pathParameters.isEmpty()) {
            new PathParameterSnippetWrapper(pathParameters).validate(operation);
            res.addAll(mapDescriptorsToModel(pathParameters, operation.getRequest().getParameters(), "path"));
        }

        List<ParameterDescriptorWithOpenAPIType> requestParameters = parameters.getRequestParameters();
        if (!requestParameters.isEmpty()) {
            new RequestParameterSnippetWrapper(requestParameters).validate(operation);
            res.addAll(mapDescriptorsToModel(requestParameters, operation.getRequest().getParameters(), "query"));
        }

        Map<String, Object> model = new HashMap<>();
        if (!res.isEmpty()) {
            model.put("parametersPresent", true);
            model.put("parameters", res);
        }

        return model;
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<HeaderDescriptor> headerDescriptors, HttpHeaders presentHeaders) {
        return headerDescriptors.stream().map(headerDescriptor -> {
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("name", headerDescriptor.getName());
            headerMap.put("description", (String) headerDescriptor.getDescription());
            headerMap.put("example", presentHeaders.getFirst(headerDescriptor.getName()));
            headerMap.put("in", "header");
            headerMap.put("type", "string");
            headerMap.put("required", Boolean.toString(!headerDescriptor.isOptional()));
            return headerMap;
        }).collect(toList());
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<ParameterDescriptorWithOpenAPIType> parametersDescriptors, Parameters presentParameters, String in) {
        return parametersDescriptors.stream().map(parameterDescriptor -> {
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("name", parameterDescriptor.getName());
            parameterMap.put("description", (String) parameterDescriptor.getDescription());
            parameterMap.put("example", presentParameters.getFirst(parameterDescriptor.getName()));
            parameterMap.put("in", in);
            parameterMap.put("type", parameterDescriptor.getType().getTypeName());
            parameterMap.put("required", Boolean.toString(!parameterDescriptor.isOptional()));
            return parameterMap;
        }).collect(toList());
    }

    private interface ElementsValidator {
        void validate(Operation operation);
    }

    private static class RequestHeaderSnippetValidator extends RequestHeadersSnippet implements ElementsValidator {
        private RequestHeaderSnippetValidator(List<HeaderDescriptor> descriptors) {
            super(descriptors);
        }

        @Override
        public void validate(Operation operation) {
            super.createModel(operation);
        }
    }

    static class RequestParameterSnippetWrapper extends RequestParametersSnippet implements ElementsValidator {

        RequestParameterSnippetWrapper(List<ParameterDescriptorWithOpenAPIType> descriptors) {
            super(descriptors.stream().map(d -> parameterWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
        }

        @Override
        public void validate(Operation operation) {
            super.createModel(operation);
        }
    }

    static class PathParameterSnippetWrapper extends PathParametersSnippet implements ElementsValidator {

        PathParameterSnippetWrapper(List<ParameterDescriptorWithOpenAPIType> descriptors) {
            super(descriptors.stream().map(d -> parameterWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
        }

        @Override
        public void validate(Operation operation) {
            super.createModel(operation);
        }
    }
}
