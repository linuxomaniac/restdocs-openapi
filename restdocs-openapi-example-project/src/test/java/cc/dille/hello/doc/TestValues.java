package cc.dille.hello.doc;

import cc.dille.hello.Greeting;

import java.util.Random;
import java.util.UUID;

public class TestValues {
    public static final Greeting greeting0 = newGreeting();
    public static final Greeting greeting1 = newGreeting();

    private static Greeting newGreeting() {
        return new Greeting(new Random().nextInt(Integer.MAX_VALUE - 1), UUID.randomUUID().toString());
    }
}
