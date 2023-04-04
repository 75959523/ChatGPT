package org.example.util;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BufferedReaderParam {

    public static String execute(HttpServletRequest request) {

        BufferedReader reader;
        StringBuilder sb = null;
        try {
            reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
            sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}
