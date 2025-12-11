package com.example.crypto_project.crypto.util;

public class MathUtils {
    
    private MathUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be  instantiated");
    }

    public static int roundToNearestOdd(double value) {
        int rounded = (int) Math.round(value);

        return (rounded % 2 == 0) ? rounded + 1 : rounded;
    }
}
