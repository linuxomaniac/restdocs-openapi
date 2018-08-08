package cc.dille.restdocs.openapi.example;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(path = "/", produces = MediaTypes.HAL_JSON_VALUE)
public class IndexController {

    /**
     * Root of the API.
     *
     * @return Returns links to API resources.
     */
    @GetMapping("/")
    public ResourceSupport index() {
        ResourceSupport index = new ResourceSupport();
        index.add(
                linkTo(NoteController.class).withRel("note"),
                linkTo(IndexController.class).withSelfRel());
        return index;
    }

}
