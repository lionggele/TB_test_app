package com.google.ar.core.codelab.function;

public class Questionnaire {
    private String question;
    private String details;
    private boolean isChecked;
    private int imageResourceID;
    private int category;

    public Questionnaire(String question, String details, boolean isChecked, int imageResourceID, int category) {
        this.question = question;
        this.details = details;
        this.isChecked = isChecked;
        this.imageResourceID = imageResourceID;
        this.category = category;
    }

    // Getter and setter methods
    public String getQuestion() {
        return question;
    }

    public String getDetails() {
        return details;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public int getImageResource() { // Getter for the image resource ID
        return imageResourceID;
    }
    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }



}
