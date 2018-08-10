package cc.dille.restdocs.openapi.example;

import lombok.Getter;
import org.springframework.hateoas.ResourceSupport;


@Getter
public class NoteResource extends ResourceSupport {
    private final Note note;

    public NoteResource(final Note note) {
        this.note = note;
    }
}
