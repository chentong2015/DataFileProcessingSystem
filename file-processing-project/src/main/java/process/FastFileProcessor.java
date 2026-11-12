package process;

import model.TagMatcher;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// 大文件的流式处理器: 读取和修改文件, 输出文件结果
// - 提取每个Item标签的ID
// - 提取每个Item标签的Pattern标签
// - 对Pattern标签格式转换
// - 输出Item标签结果到文件中
public class FastFileProcessor {

    private final TagMatcher uidMatcher;
    private boolean readingUid;
    private int uidBufferLength;
    private final byte[] uidBuffer;
    private String currentUid;

    private final byte[] patternStart;
    private final TagMatcher patternStartMatcher;
    private final TagMatcher patternEndMatcher;
    private boolean readingPattern;
    private final ByteArrayOutputStream patternBuffer; // 缓存读取的所有Pattern的数据

    private int normalBufferSize;
    private final byte[] normalBuffer; // 缓存普通字节, 达到最大缓存字节数则写入
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private final Path inputPath;
    private final String outputFileName;
    private FastFileStreamWriter xmlOutputWriter;

    public FastFileProcessor(String inoutFilepath, byte[] patternId, byte[] patternStart, byte[] patternEnd) {
        this.inputPath = Paths.get(inoutFilepath);
        this.outputFileName = inputPath.getFileName().toString();

        this.uidMatcher = new TagMatcher(patternId);
        this.uidBuffer = new byte[10];
        
        this.patternStart = patternStart;
        this.patternStartMatcher = new TagMatcher(patternStart);
        this.patternEndMatcher = new TagMatcher(patternEnd);
        this.patternBuffer = new ByteArrayOutputStream(1024);

        this.normalBuffer = new byte[BUFFER_SIZE];
    }
    
    public void process() {
        try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(inputPath), BUFFER_SIZE);
             FastFileStreamWriter outputStream = new FastFileStreamWriter(outputFileName)) {
            this.xmlOutputWriter = outputStream;
            byte[] inputBuffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = inputStream.read(inputBuffer)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    processByte(inputBuffer[i]);
                }
            }
            finish();
        } catch (IOException e) {
            throw new RuntimeException("XML processing failed", e);
        }
    }

    private void processByte(byte b) throws IOException {
        processUid(b);

        if (readingPattern) {
            patternBuffer.write(b);
            if (patternEndMatcher.match(b)) {
                processPattern();
            }
            return;
        }

        normalBuffer[normalBufferSize++] = b;

        // When find the start of <locations> tag, flush the normalBuffer before
        if (patternStartMatcher.match(b)) {
            int normalLength = normalBufferSize - patternStart.length;
            if (normalLength > 0) {
                xmlOutputWriter.writeOutput(normalBuffer, 0, normalLength);
            }
            normalBufferSize = 0;

            patternBuffer.reset();
            patternBuffer.write(patternStart, 0, patternStart.length);
            patternEndMatcher.reset();
            readingPattern = true;
            return;
        }

        flushNormalBuffer();
    }

    private void processUid(byte b) {
        if (readingUid) {
            if (b == '"') {
                currentUid = new String(uidBuffer, 0, uidBufferLength, StandardCharsets.UTF_8);
                readingUid = false;
                uidBufferLength = 0;
                return;
            }
            uidBuffer[uidBufferLength++] = b;
            return;
        }

        if (uidMatcher.match(b)) {
            readingUid = true;
            uidBufferLength = 0;
            uidMatcher.reset();
        }
    }

    // Flush normalBuffer when it reaches the BUFFER_SIZE, except the end of <locations matched characters
    private void flushNormalBuffer() throws IOException {
        if (normalBufferSize < BUFFER_SIZE) {
            return;
        }

        int locationsMatchLength = patternStartMatcher.getMatchedLength();
        int flushLength = normalBufferSize - locationsMatchLength;
        if (flushLength <= 0) {
            return;
        }
        xmlOutputWriter.writeOutput(normalBuffer, 0, flushLength);

        if (locationsMatchLength > 0) {
            System.arraycopy(normalBuffer, flushLength, normalBuffer, 0, locationsMatchLength);
        }
        normalBufferSize = locationsMatchLength;
    }

    // TODO. 读取找到的Pattern字符串, 格式化后再写入文件
    private void processPattern() throws IOException {
        byte[] result = patternBuffer.toByteArray();
        xmlOutputWriter.writeOutput(result, 0, result.length);
        patternBuffer.reset();
        patternEndMatcher.reset();
        readingPattern = false;
    }

    private void finish() throws IOException {
        if (normalBufferSize > 0) {
            xmlOutputWriter.writeOutput(normalBuffer, 0, normalBufferSize);
            normalBufferSize = 0;
        }
    }
}
