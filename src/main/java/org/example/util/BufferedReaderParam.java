package org.example.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BufferedReaderParam {

    public static String execute(HttpServletRequest request) throws IOException {

        BufferedReader reader;
        StringBuilder sb;
        reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
        sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
