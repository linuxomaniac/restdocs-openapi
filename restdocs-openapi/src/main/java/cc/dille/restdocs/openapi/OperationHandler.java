package cc.dille.restdocs.openapi;

import java.util.Map;

import org.springframework.restdocs.operation.Operation;

interface OperationHandler {

    Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters);
}
