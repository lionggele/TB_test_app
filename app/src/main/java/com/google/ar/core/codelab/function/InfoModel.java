package com.google.ar.core.codelab.function;

public class InfoModel {
    private String title;
    private boolean isExpanded;

    public InfoModel(String title) {
        this.title = title;
        this.isExpanded = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }
}
