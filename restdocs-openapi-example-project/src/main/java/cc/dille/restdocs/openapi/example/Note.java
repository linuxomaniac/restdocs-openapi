package cc.dille.restdocs.openapi.example;

public class Note {

    /**
     * Note ID
     */
    private long id;

    /**
     * Note text content
     */
    private String content;

    public Note(long id, String content) {
        this.id = id;
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Note{id:" + id + ", content: \"" + content + "\"}";
    }
}