package cc.dille.restdocs.openapi;

import java.util.List;

import org.springframework.restdocs.operation.Operation;

public interface TraitExtractor {

    List<String> extractTraits(Operation operation, OpenAPIResourceSnippetParameters parameters);
}
