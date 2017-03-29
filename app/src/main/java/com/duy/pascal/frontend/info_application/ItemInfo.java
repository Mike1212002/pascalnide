package com.duy.pascal.frontend.info_application;

public class ItemInfo {
    private String title;
    private String link;
    private String imgPath;

    public ItemInfo(String title, String link, String imgPath) {
        this.title = title;
        this.link = link;
        this.imgPath = imgPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }
}