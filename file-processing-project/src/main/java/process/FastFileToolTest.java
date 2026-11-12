package process;

import java.nio.charset.StandardCharsets;

public class FastFileToolTest {

    private static final byte[] UID_START = "uid=\"".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PATTERN_START = "<locations>".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PATTERN_END = "</locations>".getBytes(StandardCharsets.UTF_8);

    public static void main(String[] args) {
        String fullpath = "C:\\Users\\file.xml";

        System.out.println("Start processing...");
        long startTime = System.currentTimeMillis();
        FastFileProcessor fastFileProcessor = new FastFileProcessor(fullpath, UID_START, PATTERN_START, PATTERN_END);
        fastFileProcessor.process();
        System.out.println("Execution Time: " + (System.currentTimeMillis() - startTime)); // 60s
    }
}
