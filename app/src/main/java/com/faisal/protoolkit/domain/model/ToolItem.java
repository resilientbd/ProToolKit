package com.faisal.protoolkit.domain.model;

import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

/**
 * Represents a tool entry displayed on Home/Tools screens.
 */
public class ToolItem {

    private final String id;
    @StringRes
    private final int titleRes;
    @DrawableRes
    private final int iconRes;
    @IdRes
    private final int destinationId;
    private final boolean requiresPermission;

    public ToolItem(String id,
                    @StringRes int titleRes,
                    @DrawableRes int iconRes,
                    @IdRes int destinationId,
                    boolean requiresPermission) {
        this.id = id;
        this.titleRes = titleRes;
        this.iconRes = iconRes;
        this.destinationId = destinationId;
        this.requiresPermission = requiresPermission;
    }

    public String getId() {
        return id;
    }

    public int getTitleRes() {
        return titleRes;
    }

    public int getIconRes() {
        return iconRes;
    }

    public int getDestinationId() {
        return destinationId;
    }

    public boolean requiresPermission() {
        return requiresPermission;
    }
}
