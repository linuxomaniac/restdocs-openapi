package cc.dille.restdocs.openapi;

import java.io.File;

public interface OpenAPIResourceSnippetTestTrait {

    String RAML_FRAGMENT_FILE = "openapi-resource.openapi";
    String REQUEST_FILE_SUFFIX = "-request.json";
    String RESPONSE_FILE_SUFFIX = "-response.json";
    String SCHEMA_PART = "-schema";

    String getOperationName();

    File getRootOutputDirectory();

    default File generatedOpenAPIFragmentFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + RAML_FRAGMENT_FILE);
    }

    default File generatedRequestJsonFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + getOperationName() + REQUEST_FILE_SUFFIX);
    }

    default File generatedRequestSchemaFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + getOperationName() + SCHEMA_PART + REQUEST_FILE_SUFFIX);
    }

    default File generatedResponseJsonFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + getOperationName() + RESPONSE_FILE_SUFFIX);
    }

    default File generatedResponseSchemaFile() {
        return new File(getRootOutputDirectory(), getOperationName() + "/" + getOperationName() + SCHEMA_PART + RESPONSE_FILE_SUFFIX);
    }
}
