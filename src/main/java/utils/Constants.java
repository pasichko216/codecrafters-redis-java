package utils;

public class Constants {
    public static final int DEFAULT_PORT = 6379;

    public static final String PING = "PING";
    public static final String PONG = "PONG";
    public static final String OK = "OK";
    public static final String ECHO = "ECHO";

    public static final char SIMPLE_STRING = '+';
    public static final char ERROR         = '-';
    public static final char INTEGER       = ':';
    public static final char BULK_STRING   = '$';
    public static final char ARRAY         = '*';

    public static final String CRLF = "\r\n";

    public static final String PONG_RESPONSE = SIMPLE_STRING + PONG + CRLF;
    public static final String OK_RESPONSE = SIMPLE_STRING + OK + CRLF;
}
