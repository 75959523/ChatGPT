package org.chatgpt.util;

import java.text.DecimalFormat;

public class FormatPrice {

    private static final DecimalFormat df = new DecimalFormat("#.##########");

    public static String format(double price) {
        return df.format(price) + "$";
    }
}
