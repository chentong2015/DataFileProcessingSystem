package org.example.model;

// 快速匹配特定字节组成的pattern字符串数据
public class TagMatcher {

    private final byte[] pattern;
    private int matchIndex;

    public TagMatcher(byte[] pattern) {
        this.pattern = pattern;
    }

    // 逐个字节匹配, 直到完整匹配pattern字节长度
    public boolean match(byte b) {
        if (b == pattern[matchIndex]) {
            matchIndex++;
            if (matchIndex == pattern.length) {
                matchIndex = 0;
                return true; // 只有match完整标签才当会true
            }
            return false;
        }
        matchIndex = b == pattern[0] ? 1 : 0;
        return false;
    }

    public int getMatchedLength() {
        return matchIndex;
    }

    public void reset() {
        matchIndex = 0;
    }
}
