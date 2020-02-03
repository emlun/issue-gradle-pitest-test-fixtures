package org.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HelloWorldTest {

    @Test
    public void helloReturnsHello() {
        assertEquals("Hello, World!", HelloWorld.hello());
    }

}
