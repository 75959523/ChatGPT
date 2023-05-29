package org.chatgpt.util;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SaveImageFromUrl {

    public static void execute(String imageUrl, String imageName) {

        String localImagePath = "/home/service/images/"+imageName+".jpg";

        try {
            URL url = new URL(imageUrl);
            try (InputStream inputStream = url.openStream()) {
                Files.copy(inputStream, Paths.get(localImagePath), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
