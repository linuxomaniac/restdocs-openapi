package cc.dille.restdocs.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.restdocs.headers.RequestHeadersSnippet;
import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.request.ParameterDescriptor;
import org.springframework.restdocs.request.PathParametersSnippet;
import org.springframework.restdocs.request.RequestParametersSnippet;
import org.springframework.util.MultiValueMap;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

@RequiredArgsConstructor
class ParameterHandler implements OperationHandler {

    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters snippetParameters) {
        List<ParameterDescriptorWithOpenAPIType> parameters = snippetParameters.getRequestParameters();
        if (!parameters.isEmpty()) {
            List<ParameterDescriptorWithOpenAPIType> filteredParameters;

            filteredParameters = parameters.stream().filter(p -> p.getIn().equals("header")).collect(Collectors.toList());
            if (!filteredParameters.isEmpty()) {
                new RequestHeaderSnippetValidator(filteredParameters).validate(operation);
            }
            filteredParameters = parameters.stream().filter(p -> p.getIn().equals("path")).collect(Collectors.toList());
            if (!filteredParameters.isEmpty()) {
                new PathParameterSnippetWrapper(filteredParameters).validate(operation);
            }
            filteredParameters = parameters.stream().filter(p -> p.getIn().equals("query")).collect(Collectors.toList());
            if (!filteredParameters.isEmpty()) {
                new RequestParameterSnippetWrapper(filteredParameters).validate(operation);
            }

            List<Map<String, String>> descriptors = mapDescriptorsToModel(parameters, prepareParameters(operation.getRequest().getHeaders()));

            Map<String, Object> model = new HashMap<>();
            if (!descriptors.isEmpty()) {
                model.put("parametersPresent", true);
                model.put("parameters", descriptors);
            }

            return model;
        }


        return emptyMap();
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
            if(!parameterDescriptor.isIgnored()) {
                parameterMap.put("name", parameterDescriptor.getName());
                parameterMap.put("in", parameterDescriptor.getIn());
                parameterMap.put("description", (String) parameterDescriptor.getDescription());
                parameterMap.put("required", Boolean.toString(!parameterDescriptor.isOptional()));
                parameterMap.put("type", parameterDescriptor.getType().getTypeName());
                parameterMap.put("example", (parameterDescriptor.getExample() != null) ? parameterDescriptor.getExample() : presentParameters.get(parameterDescriptor.getName()));
            }
            return parameterMap;
        }).filter(m -> !m.isEmpty()).collect(toList());
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
            super(descriptors.stream().map(d -> {
                        ParameterDescriptor pd = parameterWithName(d.getName()).description(d.getDescription());
                        if (d.isIgnored()) {
                            pd.ignored();
                        }
                        return pd;
                    }
            ).collect(Collectors.toList()));
        }

        @Override
        public void validate(Operation operation) {
            super.createModel(operation);
        }
    }

    static class PathParameterSnippetWrapper extends PathParametersSnippet implements ElementsValidator {

        PathParameterSnippetWrapper(List<ParameterDescriptorWithOpenAPIType> descriptors) {
            super(descriptors.stream().map(d -> {
                        ParameterDescriptor pd = parameterWithName(d.getName()).description(d.getDescription());
                        if (d.isIgnored()) {
                            pd.ignored();
                        }
                        return pd;
                    }
            ).collect(Collectors.toList()));
        }

        @Override
        public void validate(Operation operation) {
            super.createModel(operation);
        }
    }
}
