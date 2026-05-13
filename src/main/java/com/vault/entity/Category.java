package com.vault.entity;

public enum Category {
    SOCIAL("社交"),
    WORK("工作"),
    ENTERTAINMENT("娱乐"),
    MISC("其他");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Category fromDisplayName(String name) {
        for (Category c : values()) {
            if (c.displayName.equals(name)) {
                return c;
            }
        }
        return MISC;
    }
}
