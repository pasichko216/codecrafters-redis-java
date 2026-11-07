package protocol;

import java.nio.charset.StandardCharsets;

import static protocol.Resp.*;

public final class RespWriter {

    private RespWriter() {}

    public static final String PONG = simpleString("PONG");
    public static final String OK = simpleString("OK");

    public static String simpleString(String value) {
        return SIMPLE_STRING + value + CRLF;
    }

    public static String error(ErrorCode code, String message) {
        return ERROR + code.name() + " " + message + CRLF;
    }

    public static String bulkString(String value) {
        int length = value.getBytes(StandardCharsets.UTF_8).length;
        return BULK_STRING + String.valueOf(length) + CRLF + value + CRLF;
    }
}