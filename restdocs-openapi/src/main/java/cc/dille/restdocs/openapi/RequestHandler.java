package cc.dille.restdocs.openapi;

import static java.util.Collections.emptyMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationRequest;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.RequestFieldsSnippet;
import org.springframework.util.StringUtils;

public class RequestHandler implements OperationHandler, FileNameTrait {

    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        final OperationRequest request = operation.getRequest();

        if (!StringUtils.isEmpty(request.getContentAsString())) {
            Map<String, Object> model = new HashMap<>();
            model.put("requestBodyFileName", getRequestFileName(operation.getName()));
            model.put("requestBodyPresent", true);
            model.put("contentTypeRequest", getContentTypeOrDefault(request));
            // Fill requestBodyRequired here ?
            // Fill requestBodyDescription ?
            if (!parameters.getRequestFields().isEmpty()) {
                validateRequestFieldsAndInferTypeInformation(operation, parameters);
                model.put("requestFieldsPresent", true);
                if (shouldGenerateRequestSchemaFile(operation, parameters)) {
                    model.put("requestSchemaFileName", getRequestSchemaFileName(operation.getName()));
                }
            }
            return model;
        }
        return emptyMap();
    }

    private String getContentTypeOrDefault(OperationRequest request) {
        if (request.getHeaders().getContentType() != null) {
            return request.getHeaders().getContentType().getType() + "/" + request.getHeaders().getContentType().getSubtype();
        } else {
            return APPLICATION_JSON_VALUE;
        }
    }

    private void validateRequestFieldsAndInferTypeInformation(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        new RequestFieldsSnippetWrapper(parameters.getRequestFields()).validateFieldsAndInferTypeInformation(operation);
    }

    /**
     * We need the wrapper to take advantage of the validation of fields and the inference of type information.
     * <p>
     * This is baked into {@link org.springframework.restdocs.payload.AbstractFieldsSnippet#createModel(Operation)} and is not accessible separately.
     */
    static class RequestFieldsSnippetWrapper extends RequestFieldsSnippet {

        RequestFieldsSnippetWrapper(List<FieldDescriptor> descriptors) {
            super(descriptors);
        }

        void validateFieldsAndInferTypeInformation(Operation operation) {
            super.createModel(operation);
        }
    }
}
