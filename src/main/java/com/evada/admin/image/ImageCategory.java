package com.evada.admin.image;

public enum ImageCategory {
    ROOM("rooms"),
    CONTENT("contents");

    private final String folder;

    ImageCategory(String folder) {
        this.folder = folder;
    }

    public String getFolder() {
        return folder;
    }
}