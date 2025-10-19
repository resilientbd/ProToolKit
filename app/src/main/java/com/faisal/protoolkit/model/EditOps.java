package com.faisal.protoolkit.model;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

public class EditOps {
    @SerializedName("crop")
    public List<List<Float>> crop; // [[x1,y1],[x2,y2],[x3,y3],[x4,y4]]

    @SerializedName("warp")
    public List<List<Float>> warp; // [[a,b,c],[d,e,f],[g,h,1]] - homography matrix

    @SerializedName("rotate")
    public int rotate; // 0, 90, 180, 270

    @SerializedName("filter")
    public Filter filter;

    @SerializedName("denoise")
    public float denoise; // 0.0 to 1.0

    @SerializedName("deskew")
    public float deskew; // -45.0 to 45.0 degrees

    public EditOps() {
        this.rotate = 0;
        this.denoise = 0.0f;
        this.deskew = 0.0f;
        this.filter = new Filter();
    }

    public static class Filter {
        @SerializedName("mode")
        public String mode; // ORIGINAL, GRAY, BW, COLOR_BOOST

        @SerializedName("contrast")
        public float contrast; // 0.0 to 2.0, default 1.0

        @SerializedName("brightness")
        public float brightness; // -1.0 to 1.0, default 0.0

        @SerializedName("sharpen")
        public float sharpen; // 0.0 to 1.0, default 0.0

        public Filter() {
            this.mode = "ORIGINAL";
            this.contrast = 1.0f;
            this.brightness = 0.0f;
            this.sharpen = 0.0f;
        }
    }

    // Helper methods for common operations
    public void setCrop(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4) {
        this.crop = Arrays.asList(
            Arrays.asList(x1, y1),
            Arrays.asList(x2, y2),
            Arrays.asList(x3, y3),
            Arrays.asList(x4, y4)
        );
    }

    public boolean hasCrop() {
        return crop != null && crop.size() == 4;
    }

    public boolean hasRotation() {
        return rotate != 0;
    }

    public boolean hasFilter() {
        return filter != null && 
               (filter.mode != null && !filter.mode.equals("ORIGINAL")) ||
               filter.contrast != 1.0f ||
               filter.brightness != 0.0f ||
               filter.sharpen != 0.0f;
    }

    public boolean hasWarp() {
        return warp != null && warp.size() == 3;
    }

    public boolean hasDenoise() {
        return denoise > 0.0f;
    }

    public boolean hasDeskew() {
        return Math.abs(deskew) > 0.0f;
    }

    public boolean hasAnyEditOps() {
        return hasCrop() || hasRotation() || hasFilter() || hasWarp() || hasDenoise() || hasDeskew();
    }

    public void resetToDefaults() {
        this.crop = null;
        this.warp = null;
        this.rotate = 0;
        this.denoise = 0.0f;
        this.deskew = 0.0f;
        if (this.filter == null) {
            this.filter = new Filter();
        } else {
            this.filter.mode = "ORIGINAL";
            this.filter.contrast = 1.0f;
            this.filter.brightness = 0.0f;
            this.filter.sharpen = 0.0f;
        }
    }
}