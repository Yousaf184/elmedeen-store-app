package com.example.yousafkhan.elmedeenappstore.Models;

public class App {

    private String name_eng;
    private String name_urd;
    private String name_arb;
    private String description_eng;
    private String description_urd;
    private String description_arb;
    private String apk_size;
    private String packageName;
    private String titleImageURL;
    private String versionURL;
    private String downloadURL;
    private String screenshot1URL;
    private String screenshot2URL;

    public App(String name_eng, String name_urd, String name_arb, String description_eng,
               String description_urd, String description_arb, String apk_size, String packageName,
               String titleImageURL, String versionURL, String downloadURL, String screenshot1URL,
               String screenshot2URL) {

        this.name_eng = name_eng;
        this.name_urd = name_urd;
        this.name_arb = name_arb;
        this.description_eng = description_eng;
        this.description_urd = description_urd;
        this.description_arb = description_arb;
        this.apk_size = apk_size;
        this.packageName = packageName;
        this.titleImageURL = titleImageURL;
        this.versionURL = versionURL;
        this.downloadURL = downloadURL;
        this.screenshot1URL = screenshot1URL;
        this.screenshot2URL = screenshot2URL;
    }

    public String getAppNameEng() {
        return name_eng;
    }

    public String getAppNameUrd() {
        return name_urd;
    }

    public String getAppNameArb() {
        return name_arb;
    }

    public String getAppDescriptionEng() {
        return description_eng;
    }

    public String getAppDescriptionUrd() {
        return description_urd;
    }

    public String getAppDescriptionArb() {
        return description_arb;
    }

    public String getApk_size() {
        return apk_size;
    }

    public String getAppPackageName() {
        return packageName;
    }

    public String getAppTitleImageURL() {
        return titleImageURL;
    }

    public String getAppVersionURL() {
        return versionURL;
    }

    public String getAppDownloadURL() {
        return downloadURL;
    }

    public String getAppScreenshot1URL() {
        return screenshot1URL;
    }

    public String getAppScreenshot2URL() {
        return screenshot2URL;
    }

}
