package com.faisal.protoolkit.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.faisal.protoolkit.model.EditOps;
import java.lang.reflect.Type;

public class EditOpsUtil {
    private static final Gson gson = new Gson();
    private static final Type editOpsType = new TypeToken<EditOps>(){}.getType();

    public static String serialize(EditOps editOps) {
        if (editOps == null) {
            editOps = new EditOps(); // Create default
        }
        return gson.toJson(editOps);
    }

    public static EditOps deserialize(String editOpsJson) {
        if (editOpsJson == null || editOpsJson.isEmpty()) {
            return new EditOps(); // Return default
        }
        try {
            return gson.fromJson(editOpsJson, editOpsType);
        } catch (Exception e) {
            // If deserialization fails, return default
            return new EditOps();
        }
    }

    public static EditOps merge(EditOps base, EditOps override) {
        if (base == null) base = new EditOps();
        if (override == null) return base;

        EditOps result = new EditOps();
        
        // Copy crop
        result.crop = override.crop != null ? override.crop : base.crop;
        
        // Copy warp
        result.warp = override.warp != null ? override.warp : base.warp;
        
        // Copy rotation
        result.rotate = override.rotate != 0 ? override.rotate : base.rotate;
        
        // Copy denoise
        result.denoise = override.denoise > 0 ? override.denoise : base.denoise;
        
        // Copy deskew
        result.deskew = Math.abs(override.deskew) > 0 ? override.deskew : base.deskew;
        
        // Copy filter
        result.filter = new EditOps.Filter();
        if (override.filter != null) {
            result.filter.mode = override.filter.mode != null ? override.filter.mode : (base.filter != null ? base.filter.mode : "ORIGINAL");
            result.filter.contrast = override.filter.contrast != 1.0f ? override.filter.contrast : (base.filter != null ? base.filter.contrast : 1.0f);
            result.filter.brightness = override.filter.brightness != 0.0f ? override.filter.brightness : (base.filter != null ? base.filter.brightness : 0.0f);
            result.filter.sharpen = override.filter.sharpen != 0.0f ? override.filter.sharpen : (base.filter != null ? base.filter.sharpen : 0.0f);
        } else if (base.filter != null) {
            result.filter.mode = base.filter.mode;
            result.filter.contrast = base.filter.contrast;
            result.filter.brightness = base.filter.brightness;
            result.filter.sharpen = base.filter.sharpen;
        }

        return result;
    }

    public static boolean isDefault(EditOps editOps) {
        if (editOps == null) return true;
        
        return editOps.crop == null && 
               editOps.warp == null && 
               editOps.rotate == 0 &&
               editOps.denoise == 0.0f &&
               editOps.deskew == 0.0f &&
               (editOps.filter == null || 
                (editOps.filter.mode != null && editOps.filter.mode.equals("ORIGINAL") &&
                 editOps.filter.contrast == 1.0f &&
                 editOps.filter.brightness == 0.0f &&
                 editOps.filter.sharpen == 0.0f));
    }
}