package cc.dille.restdocs.openapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.headers.HeaderDescriptor;
import org.springframework.restdocs.headers.ResponseHeadersSnippet;
import org.springframework.restdocs.operation.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
class ResponseHeaderHandler implements OperationHandler {

    @Override
    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        List<HeaderDescriptor> headers = parameters.getResponseHeaders();
        if (!headers.isEmpty()) {
            new ResponseHeaderHandler.ResponseHeaderSnippetValidator(headers).validateHeaders(operation);
            Map<String, Object> model = new HashMap<>();
            model.put("responseHeadersPresent", true);
            model.put("responseHeaders", mapDescriptorsToModel(headers, operation.getResponse().getHeaders()));
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

    private static class ResponseHeaderSnippetValidator extends ResponseHeadersSnippet implements HeadersValidator {
        private ResponseHeaderSnippetValidator(List<HeaderDescriptor> descriptors) {
            super(descriptors);
        }

        @Override
        public void validateHeaders(Operation operation) {
            super.createModel(operation);
        }
    }
}
