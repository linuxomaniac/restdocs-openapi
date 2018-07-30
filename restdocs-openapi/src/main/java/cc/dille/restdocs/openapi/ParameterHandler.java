package cc.dille.restdocs.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

@RequiredArgsConstructor
class ParameterHandler implements OperationHandler {

    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        List<Map<String, String>> res = new ArrayList<>();

        List<ParameterDescriptorWithOpenAPIType> headers = parameters.getRequestHeaders();
        if (!headers.isEmpty()) {
            new RequestHeaderSnippetValidator(headers).validate(operation);
            res.addAll(mapDescriptorsToModel(headers, prepareParameters(operation.getRequest().getHeaders())));
        }

        List<ParameterDescriptorWithOpenAPIType> pathParameters = parameters.getPathParameters();
        if (!pathParameters.isEmpty()) {
            new PathParameterSnippetWrapper(pathParameters).validate(operation);
            res.addAll(mapDescriptorsToModel(pathParameters, prepareParameters(operation.getRequest().getParameters())));
        }

        List<ParameterDescriptorWithOpenAPIType> requestParameters = parameters.getRequestParameters();
        if (!requestParameters.isEmpty()) {
            new RequestParameterSnippetWrapper(requestParameters).validate(operation);
            res.addAll(mapDescriptorsToModel(requestParameters, prepareParameters(operation.getRequest().getParameters())));
        }

        Map<String, Object> model = new HashMap<>();
        if (!res.isEmpty()) {
            model.put("parametersPresent", true);
            model.put("parameters", res);
        }

        return model;
    }

    private Map<String, String> prepareParameters(MultiValueMap<String, String> rawParameters) {
        Map<String, String> parameters = new HashMap<String, String>();

        for (String str : rawParameters.keySet()) {
            parameters.put(str, rawParameters.getFirst(str));
        }

        return parameters;
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<ParameterDescriptorWithOpenAPIType> parametersDescriptors, Map<String, String> presentParameters) {
        return parametersDescriptors.stream().map(parameterDescriptor -> {
            Map<String, String> parameterMap = new HashMap<>();
            parameterMap.put("name", parameterDescriptor.getName());
            parameterMap.put("in", parameterDescriptor.getIn());
            parameterMap.put("description", (String) parameterDescriptor.getDescription());
            parameterMap.put("required", Boolean.toString(!parameterDescriptor.isOptional()));
            parameterMap.put("type", parameterDescriptor.getType().getTypeName());
            parameterMap.put("example", (parameterDescriptor.getExample() != null)?parameterDescriptor.getExample():presentParameters.get(parameterDescriptor.getName()));
            return parameterMap;
        }).collect(toList());
    }

    private interface ElementsValidator {
        void validate(Operation operation);
    }

    private static class RequestHeaderSnippetValidator extends RequestHeadersSnippet implements ElementsValidator {
        private RequestHeaderSnippetValidator(List<ParameterDescriptorWithOpenAPIType> descriptors) {
            super(descriptors.stream().map(d -> headerWithName(d.getName())
                    .description(d.getDescription()))
                    .collect(Collectors.toList()));
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
