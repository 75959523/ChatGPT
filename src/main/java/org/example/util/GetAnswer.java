package org.example.util;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetAnswer {

    public static String extractContent(String input) {

        List<String> contents = new ArrayList<String>();
        Pattern pattern = Pattern.compile("\"content\":\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            contents.add(matcher.group(1));
        }
        return String.join("", contents);
    }
}
