package com.launcher.mummu.cabclient.models;


import java.io.Serializable;

/**
 * Created by muhammed on 2/22/2017.
 */

public class MessageModel implements Serializable {


    private String title;


    private String message;

    private String imageUrl;


    private String buttonText;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void setButtonText(String buttonText) {
        this.buttonText = buttonText;
    }
}
