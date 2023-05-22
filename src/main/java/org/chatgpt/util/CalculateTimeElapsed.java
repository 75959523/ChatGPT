package org.chatgpt.util;

import java.text.DecimalFormat;

public class CalculateTimeElapsed {

    private static final DecimalFormat format = new DecimalFormat("0.000");

    public static String format(long begin) {
        return format.format((double)(System.currentTimeMillis() - begin) / 1000);
    }
}
