package dev.olive.event.annotations;

public final class Priority
{
    public static final byte HIGHEST = 0;
    public static final byte HIGH = 1;
    public static final byte MEDIUM = 2;
    public static final byte LOW = 3;
    public static final byte LOWEST = 4;
    public static final byte VERYVERYLOW = 100;
    public static final byte VERY_LOW = 0;
    public static final byte[] VALUE_ARRAY;

    static {
        VALUE_ARRAY = new byte[] { 0, 1, 2, 3, 4, 100 };
    }
}
