package com.faisal.protoolkit.util;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.content.Context;
import java.io.IOException;

public class ImageUtils {

    public static Bitmap getBitmapFromUri(Context context, Uri uri) throws IOException {
        Bitmap bitmap = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // API 28+ (Pie)
            ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
            bitmap = ImageDecoder.decodeBitmap(source);
        } else {
            // Older versions
            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        }
        return bitmap;
    }
}
