package org.chatgpt.util;

import java.text.DecimalFormat;

public class FormatCost {

    private static final DecimalFormat df = new DecimalFormat("#.##########");

    public static String format(double cost) {
        return df.format(cost) + "$";
    }
}
