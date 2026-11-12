package org.example.counter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

public class FastFileCounterTest {

    private static final byte[] WORD_PATTERN = "<record".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) throws IOException {
        String fullpath = "C:\\Users\\file.xml";

        long startTime = System.currentTimeMillis();
        FastFileCounter fileCounter = new FastFileCounter(Paths.get(fullpath), WORD_PATTERN);
        System.out.println(fileCounter.countPattern());
        System.out.println("Execution Time: " + (System.currentTimeMillis() - startTime)); // 17s
    }
}
