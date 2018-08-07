package cc.dille.hello;

import cc.dille.hello.status.NoContentException;
import cc.dille.hello.status.ResourceNotFoundException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(path = "/greeting", produces = MediaTypes.HAL_JSON_VALUE)
public class GreetingController {

    private Map<Long, Greeting> m = new HashMap<Long, Greeting>();

    /**
     * Get all the Greetings recorded.
     *
     * @return A collection of the recorded Greetings is returned.
     */
    @GetMapping()
    public Collection<Greeting> indexGreeting() {
        return m.values();
    }

    /**
     * Get a Greeting by id.
     *
     * @param id The unique identifier of the {@link Greeting} to get
     * @return The matching Greeting is returned if existing.
     */
    @GetMapping("/{id}")
    Greeting getGreeting(@PathVariable long id) {
        Greeting g = m.get(id);

        if (g == null) {
            throw new ResourceNotFoundException();
        }

        return g;
    }

    /**
     * Deletes a Greeting by id.
     *
     * @param id ID of the greeting.
     *           Deletes a greeting.
     */
    @DeleteMapping("/{id}")
    void deleteGreeting(@PathVariable long id) {
        if (!m.containsKey(id)) {
            throw new ResourceNotFoundException();
        }

        m.remove(id);

         throw new NoContentException();
    }

    /**
     * Adds a new greeting element.
     *
     * @param greeting Greeting Content
     * @return reponse
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = {MediaTypes.HAL_JSON_VALUE})
    public ResourceSupport postGreeting(@Valid @RequestBody Greeting greeting) throws MalformedURLException {
        m.put(greeting.getId(), greeting);

        ResourceSupport index = new ResourceSupport();
        index.add(linkTo(GreetingController.class).slash(String.valueOf(greeting.getId())).withRel("self"));
        index.add(linkTo(GreetingController.class).withRel("greeting"));
        return index;
    }
}
