package protocol;

public final class Resp {

    private Resp() {}

    public static final char CR = '\r';
    public static final char LF = '\n';
    public static final String CRLF = "\r\n";

    public static final char SIMPLE_STRING = '+';
    public static final char ERROR         = '-';
    public static final char INTEGER       = ':';
    public static final char BULK_STRING   = '$';
    public static final char ARRAY         = '*';
}