package com.nigmatech.mediaplayerforbookpages;

class ListViewData {

    private String mediaName;
    private String mediaNumber;

    ListViewData(String MediaName, String mediaNumber) {
        this.mediaName = MediaName;
        this.mediaNumber = mediaNumber;
    }

    String getMediaName() {
        return mediaName;
    }
}