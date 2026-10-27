package dev.rustify;

public final class NativeBridge {
    private NativeBridge() {}
    static native int abi();
    static native String version();
    static native int selfTest();
    static native int ping(int value);
    static native long[] pack(int[] values, int bits);
}
