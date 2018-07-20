package cc.dille.restdocs.openapi;

import static java.util.Collections.emptyMap;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.restdocs.operation.Operation;
import org.springframework.restdocs.operation.OperationResponse;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.ResponseFieldsSnippet;
import org.springframework.util.StringUtils;

public class ResponseHandler implements OperationHandler, FileNameTrait {

    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        final OperationResponse response = operation.getResponse();
        if (!StringUtils.isEmpty(response.getContentAsString())) {
            Map<String, Object> model = new HashMap<>();
            model.put("responseBodyFileName", getResponseFileName(operation.getName()));
            model.put("responseBodyPresent", true);
            model.put("contentTypeResponse", getContentTypeOrDefault(response));
            if (!parameters.getResponseFields().isEmpty()) {
                validateResponseFieldsAndInferTypeInformation(operation, parameters);
                model.put("responseFieldsPresent", true);
                if (shouldGenerateResponseSchemaFile(operation, parameters)) {
                    model.put("responseSchemaFileName", getResponseSchemaFileName(operation.getName()));
                }
            }
            return model;
        }
        return emptyMap();
    }

    private String getContentTypeOrDefault(OperationResponse response) {
        if (response.getHeaders().getContentType() != null) {
            return response.getHeaders().getContentType().getType() + "/" + response.getHeaders().getContentType().getSubtype();
        } else {
            return APPLICATION_JSON_VALUE;
        }
    }

    private void validateResponseFieldsAndInferTypeInformation(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        new ResponseFieldsSnippetWrapper(parameters.getResponseFieldsWithLinks()).validateFieldsAndInferTypeInformation(operation);
    }

    /**
     * We need the wrapper to take advantage of the validation of fields and the inference of type information.
     *
     * This is baked into {@link org.springframework.restdocs.payload.AbstractFieldsSnippet#createModel(Operation)} and is not accessible separately.
     */
    static class ResponseFieldsSnippetWrapper extends ResponseFieldsSnippet {

        ResponseFieldsSnippetWrapper(List<FieldDescriptor> descriptors) {
            super(descriptors);
        }

        void validateFieldsAndInferTypeInformation(Operation operation) {
            super.createModel(operation);
        }
    }
}
