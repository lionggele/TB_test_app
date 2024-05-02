package com.google.ar.core.codelab.function;

public class PageContent {
    public enum ContentType {
        TEXT, IMAGE
    }

    private final ContentType type;
    private final String text;
    private final int imageResId;

    private PageContent(ContentType type, String text, int imageResId) {
        this.type = type;
        this.text = text;
        this.imageResId = imageResId;
    }

    public static PageContent text(String text) {
        return new PageContent(ContentType.TEXT, text, 0);
    }

    public static PageContent image(int imageResId) {
        return new PageContent(ContentType.IMAGE, null, imageResId);
    }

    public ContentType getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public int getImageResId() {
        return imageResId;
    }
}
