package cc.dille.hello;

import javax.validation.constraints.NotNull;

public class Greeting {

    /**
     * Greeting ID
     */
    @NotNull
    private long id;

    /**
     * Greeting text content
     */
    private String content;

    public Greeting() {
        super();
    }

    public Greeting(long id, String content) {
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
        return "Greeting{id:" + id + ", content; \"" + content + "\"}";
    }
}