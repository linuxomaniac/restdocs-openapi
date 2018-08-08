package cc.dille.restdocs.openapi.example.doc;

import cc.dille.restdocs.openapi.example.Note;

import java.util.Random;
import java.util.UUID;

public class TestValues {
    public static final Note NOTE_0 = newNote();
    public static final Note NOTE_1 = newNote();

    private static Note newNote() {
        return new Note(new Random().nextInt(Integer.MAX_VALUE - 1), UUID.randomUUID().toString());
    }
}
