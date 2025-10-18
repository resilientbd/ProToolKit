package com.faisal.protoolkit.domain.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

/**
 * Represents a simple suggestion displayed in File Tools.
 */
public class SuggestionItem {

    @StringRes
    private final int titleRes;
    @StringRes
    private final int descriptionRes;
    private final String title;
    private final String description;
    @DrawableRes
    private final int iconRes;
    private final boolean usesResourceIds;

    // Constructor for resource-based items
    public SuggestionItem(@StringRes int titleRes, @StringRes int descriptionRes, @DrawableRes int iconRes) {
        this.titleRes = titleRes;
        this.descriptionRes = descriptionRes;
        this.title = null;
        this.description = null;
        this.iconRes = iconRes;
        this.usesResourceIds = true;
    }

    // Constructor for string-based items
    public SuggestionItem(String title, String description, @DrawableRes int iconRes) {
        this.titleRes = 0;
        this.descriptionRes = 0;
        this.title = title;
        this.description = description;
        this.iconRes = iconRes;
        this.usesResourceIds = false;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public int getDescriptionRes() {
        return descriptionRes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getIconRes() {
        return iconRes;
    }

    public boolean usesResourceIds() {
        return usesResourceIds;
    }
}