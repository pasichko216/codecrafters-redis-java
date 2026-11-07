package protocol;

import exception.ProtocolException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static protocol.Resp.*;

public class RespParser {

    private static final int MAX_LENGTH = 100_000_000;

    public List<String> parse(ByteBuffer buffer) {
        int start = buffer.position();
        List<String> result = tryParse(buffer);
        if (result == null) {
            buffer.position(start);
        }
        return result;
    }

    private List<String> tryParse(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return null;
        }
        char type = (char) buffer.get();
        if (type != ARRAY) {
            throw new ProtocolException("expected '" + ARRAY + "', got '" + type + "'");
        }

        Integer count = readNumber(buffer);
        if (count == null) {
            return null;
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String part = readBulkString(buffer);
            if (part == null) {
                return null;
            }
            parts.add(part);
        }
        return parts;
    }

    private String readBulkString(ByteBuffer buffer) {
        if (!buffer.hasRemaining()) {
            return null;
        }
        char type = (char) buffer.get();
        if (type != BULK_STRING) {
            throw new ProtocolException("expected '" + BULK_STRING + "', got '" + type + "'");
        }

        Integer length = readNumber(buffer);
        if (length == null || buffer.remaining() < length + 2) {
            return null;
        }

        byte[] bytes = new byte[length];
        buffer.get(bytes);
        if (buffer.get() != CR || buffer.get() != LF) {
            throw new ProtocolException("bulk string must end with CRLF");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private Integer readNumber(ByteBuffer buffer) {
        int value = 0;
        boolean hasDigits = false;
        while (buffer.hasRemaining()) {
            byte b = buffer.get();
            if (b == CR) {
                if (!buffer.hasRemaining()) {
                    return null;
                }
                if (buffer.get() != LF) {
                    throw new ProtocolException("expected LF after CR");
                }
                if (!hasDigits) {
                    throw new ProtocolException("empty number");
                }
                return value;
            }
            if (b < '0' || b > '9') {
                throw new ProtocolException("unexpected character in number: " + (char) b);
            }
            value = value * 10 + (b - '0');
            if (value > MAX_LENGTH) {
                throw new ProtocolException("number too large");
            }
            hasDigits = true;
        }
        return null;
    }
}