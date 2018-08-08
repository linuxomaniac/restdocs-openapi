package cc.dille.restdocs.openapi.example;

import cc.dille.restdocs.openapi.example.status.NoContentException;
import cc.dille.restdocs.openapi.example.status.ResourceNotFoundException;
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
@RequestMapping(path = "/note", produces = MediaTypes.HAL_JSON_VALUE)
public class NoteController {

    private Map<Long, Note> m = new HashMap<Long, Note>();

    /**
     * Get all the Greetings recorded.
     *
     * @return A collection of the recorded Greetings is returned.
     */
    @GetMapping()
    public Collection<Note> indexGreeting() {
        return m.values();
    }

    /**
     * Get a Note by id.
     *
     * @param id The unique identifier of the {@link Note} to get
     * @return The matching Note is returned if existing.
     */
    @GetMapping("/{id}")
    Note getGreeting(@PathVariable long id) {
        Note g = m.get(id);

        if (g == null) {
            throw new ResourceNotFoundException();
        }

        return g;
    }

    /**
     * Deletes a Note by id.
     *
     * @param id ID of the note.
     *           Deletes a note.
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
     * Adds a new note element.
     *
     * @param note Note Content
     * @return reponse
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = {MediaTypes.HAL_JSON_VALUE})
    public ResourceSupport postGreeting(@Valid @RequestBody Note note) throws MalformedURLException {
        m.put(note.getId(), note);

        ResourceSupport index = new ResourceSupport();
        index.add(linkTo(NoteController.class).slash(String.valueOf(note.getId())).withRel("self"));
        index.add(linkTo(NoteController.class).withRel("note"));
        return index;
    }
}
