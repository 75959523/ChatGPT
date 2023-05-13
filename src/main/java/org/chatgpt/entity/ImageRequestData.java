package org.chatgpt.entity;

public class ImageRequestData {
    private String prompt;
    private int n;
    private String size;

    public ImageRequestData (String prompt, int n, String size) {
        this.prompt = prompt;
        this.n = n;
        this.size = size;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
