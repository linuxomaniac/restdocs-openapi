package cc.dille.restdocs.openapi.example;

import cc.dille.restdocs.openapi.example.status.NoContentException;
import cc.dille.restdocs.openapi.example.status.ResourceNotFoundException;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
@RequestMapping(path = "/note", produces = MediaTypes.HAL_JSON_VALUE)
public class NoteController {

    private Map<Long, Note> m = new HashMap<Long, Note>();

    /**
     * Get all the Notess recorded.
     *
     * @return A collection of the recorded Notes is returned.
     */
    @GetMapping()
    public Collection<Note> index() {
        return m.values();
    }

    /**
     * Get a Note by id.
     *
     * @param id The unique identifier of the {@link Note} to get
     * @return The matching Note is returned if existing.
     */
    @GetMapping("/{id}")
    ResponseEntity<NoteResource> get(@PathVariable long id) {
        Note g = m.get(id);

        if (g == null) {
            throw new ResourceNotFoundException();
        }

        return ResponseEntity.ok(new NoteResource(g));
    }

    /**
     * Deletes a Note by id.
     *
     * @param id ID of the note.
     *           Deletes a note.
     */
    @DeleteMapping("/{id}")
    void delete(@PathVariable long id) {
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
    public ResponseEntity post(@Valid @RequestBody Note note) throws URISyntaxException {
        m.put(note.getId(), note);

        return ResponseEntity.created(new URI(linkTo(NoteController.class).slash(String.valueOf(note.getId())).withRel("self").getHref())).body(note);
    }
}
