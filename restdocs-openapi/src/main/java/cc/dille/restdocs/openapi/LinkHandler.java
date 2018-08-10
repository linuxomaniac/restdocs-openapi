package cc.dille.restdocs.openapi;


import org.springframework.restdocs.hypermedia.HypermediaDocumentation;
import org.springframework.restdocs.hypermedia.LinkDescriptor;
import org.springframework.restdocs.hypermedia.LinksSnippet;
import org.springframework.restdocs.operation.Operation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
            List<Map<String, Object>> descriptors = mapDescriptorsToModel(links);
            if (!descriptors.isEmpty()) {
                model.put("responseLinksPresent", true);
                model.put("links", descriptors);
            }
            return model;
        }

        return emptyMap();
    }

    private List<Map<String, Object>> mapDescriptorsToModel(List<LinkDescriptorWithOpenAPIType> linksDescriptors) {
        return linksDescriptors.stream().map(linkDescriptor -> {
            Map<String, Object> linkMap = new HashMap<>();
            if (!linkDescriptor.isIgnored()) {
                linkMap.put("name", linkDescriptor.getRel());
                linkMap.put("description", linkDescriptor.getDescription());
                linkMap.put("operationId", linkDescriptor.getOperationId());

                if(!linkDescriptor.getParameters().isEmpty()) {
                    List<Map<String, String>> linkParameters = linkDescriptor.getParameters().entrySet().stream().map(
                            e -> {
                                Map<String, String> map = new HashMap<>();
                                map.put("name", e.getKey());
                                map.put("location", e.getValue());
                                return map;
                            }
                    ).collect(Collectors.toList());

                    linkMap.put("linkParametersPresent", true);
                    linkMap.put("parameters", linkParameters);
                }
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


