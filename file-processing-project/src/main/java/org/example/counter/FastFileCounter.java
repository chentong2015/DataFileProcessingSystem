package org.example.counter;

import org.example.model.TagMatcher;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

// 持续从文件中流式读取Buffer数据, 循环处理字节
public class FastFileCounter {

    private final Path filepath;
    private final byte[] pattern;
    private static final int BUFFER_SIZE = 1024 * 1024;

    public FastFileCounter(Path filepath, byte[] pattern) {
        this.pattern = pattern;
        this.filepath = filepath;
        if (!Files.isRegularFile(filepath)) {
            throw new RuntimeException("Input file not found: " + filepath);
        }
    }

    public int countRecordsByByte() throws IOException {
        int count = 0;
        TagMatcher matcher = new TagMatcher(pattern);
        byte[] buffer = new byte[BUFFER_SIZE];
        try (InputStream in = new BufferedInputStream(Files.newInputStream(filepath), BUFFER_SIZE)) {
            int len;
            while ((len = in.read(buffer)) != -1) {
                for (int i = 0; i < len; i++) {
                    if (matcher.match(buffer[i])) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}