package org.chatgpt.util;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class SaveImageFromUrl {

    public static void execute(String imageUrl, String imageName) {

        //String localImagePath = "C:/Users/long/Desktop/videos/"+imageName+".jpg";
        String localImagePath = "/home/service/images/"+imageName+".jpg";

        try {
            // 创建URL对象
            URL url = new URL(imageUrl);
            // 通过URL对象打开网络连接并获取输入流
            try (InputStream inputStream = url.openStream()) {
                // 将输入流中的内容复制到指定的本地文件路径
                Files.copy(inputStream, Paths.get(localImagePath), StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
