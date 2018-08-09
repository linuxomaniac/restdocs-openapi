package cc.dille.restdocs.openapi;


import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.operation.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel;

/**
 * Handle {@link cc.dille.restdocs.openapi.LinkDescriptorWithOpenAPIType}
 * <p>
 * Links are added to the model as part of the response in {@link ResponseHandler}.
 */
public class LinkHandler implements OperationHandler {

    @Override
    public Map<String, Object> generateModel(Operation operation, OpenAPIResourceSnippetParameters parameters) {
        List<LinkDescriptorWithOpenAPIType> links = parameters.getLinks();
        if (!links.isEmpty()) {
            new LinkSnippetWrapper(parameters.getLinks()).validateLinks(operation);

            Map<String, Object> model = new HashMap<>();
            List<Map<String, String>> descriptors = mapDescriptorsToModel(links);
            if (!descriptors.isEmpty()) {
                model.put("responseLinksPresent", true);
                model.put("links", descriptors);
            }
            return model;
        }

        return emptyMap();
    }

    private List<Map<String, String>> mapDescriptorsToModel(List<LinkDescriptorWithOpenAPIType> linksDescriptors) {
        return linksDescriptors.stream().map(linkDescriptor -> {
            Map<String, String> linkMap = new HashMap<>();
            if (!linkDescriptor.isIgnored()) {
                linkMap.put("name", linkDescriptor.getRel());
                linkMap.put("description", (String) linkDescriptor.getDescription());
                linkMap.put("operationId", linkDescriptor.getOperationId());
            }
            return linkMap;
        }).filter(m -> !m.isEmpty()).collect(toList());
    }

    static class LinkSnippetWrapper extends LinksSnippet {

        LinkSnippetWrapper(List<LinkDescriptorWithOpenAPIType> descriptors) {
            super(HypermediaDocumentation.halLinks(), descriptors.stream().map(d -> {
                        LinkDescriptor ld = linkWithRel(d.getRel()).description(d.getDescription());
                        if (d.isIgnored()) {
                            ld.ignored();
                        }
                        return ld;
                    }
            ).collect(Collectors.toList()));
        }

        /**
         * delegate to createModel which will validate the links.
         * That is checking that all documented links exist in the response and also if all existing links are documented
         *
         * @param operation
         */
        void validateLinks(Operation operation) {
            super.createModel(operation);
        }
    }
}


