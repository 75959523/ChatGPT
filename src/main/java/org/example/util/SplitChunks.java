package org.example.util;

import java.util.ArrayList;
import java.util.List;

public class SplitChunks {

    public static List<String> execute(String input){

        List<String> parts = new ArrayList<>();
        int startIndex = 0;

        while (startIndex != -1) {
            startIndex = input.indexOf("data:", startIndex);
            if (startIndex == -1) {
                break;
            }
            int nextIndex = input.indexOf("data:", startIndex + 1);

            if (nextIndex != -1) {
                parts.add(input.substring(startIndex, nextIndex));
            } else {
                parts.add(input.substring(startIndex));
            }
            startIndex = nextIndex;
        }
        return parts;
    }
}
