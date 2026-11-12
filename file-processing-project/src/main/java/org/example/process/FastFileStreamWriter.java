package org.example.process;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// 大数据快速写入到大文件中, 字节写入
public class FastFileStreamWriter implements Closeable {

    private int outputBufferLength;
    private final byte[] outputBuffer;
    private static final int BUFFER_SIZE = 2 * 1024 * 1024;

    private final OutputStream outputStream;

    public FastFileStreamWriter(String fileName) throws IOException {
        this.outputBuffer = new byte[BUFFER_SIZE];
        this.outputStream = new BufferedOutputStream(Files.newOutputStream(buildOutputPath(fileName),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), BUFFER_SIZE);
    }

    // 在当前目录文件下面输出文件
    private Path buildOutputPath(String xmlFileName) {
        String datetime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMddHHmmss"));
        String outputName = datetime + "_" + xmlFileName;
        return Path.of(outputName);
    }

    // Write to outputStream only when reaches the BUFFER_SIZE
    public void writeOutput(byte[] data, int offset, int length) throws IOException {
        while (length > 0) {
            int available = BUFFER_SIZE - outputBufferLength;
            int copyLength = Math.min(available, length);
            System.arraycopy(data, offset, outputBuffer, outputBufferLength, copyLength);

            outputBufferLength += copyLength;
            offset += copyLength;
            length -= copyLength;
            if (outputBufferLength == BUFFER_SIZE) {
                flushOutput();
            }
        }
    }

    public void flushOutput() throws IOException {
        if (outputBufferLength > 0) {
            this.outputStream.write(outputBuffer, 0, outputBufferLength);
            outputBufferLength = 0;
        }
    }

    // Write the last remaining outputBuffer before closing
    @Override
    public void close() throws IOException {
        flushOutput();
        if (outputStream != null) {
            outputStream.close();
        }
    }
}