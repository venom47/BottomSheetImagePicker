package io.github.venomfortyseven.bottomsheetimagepicker.image;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Environment;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageCompressor {


    private int maxWidth;
    private int maxHeight;
    private Bitmap.CompressFormat compressFormat;
    private int quality;


    public ImageCompressor(Builder builder) {
        maxWidth = builder.maxWidth;
        maxHeight = builder.maxHeight;
        compressFormat = builder.compressFormat;
        quality = builder.quality;
    }


    public File compress(File imageFile) throws IOException {
        return compress(imageFile, maxWidth, maxHeight, compressFormat, quality);
    }


    private File compress(@NonNull File imageFile,
                          @IntRange(from = 1, to = Integer.MAX_VALUE) int maxWidth,
                          @IntRange(from = 1, to = Integer.MAX_VALUE) int maxHeight,
                          @NonNull Bitmap.CompressFormat compressFormat,
                          @IntRange(from = 1, to = 100) int quality) throws IOException {
        FileOutputStream fileOutputStream = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        if (!dir.exists())
            if (!dir.mkdirs())
                return null;
        String suffix = compressFormat == Bitmap.CompressFormat.PNG ? ".png" : ".jpg";
        File compressedFile = File.createTempFile(timeStamp, suffix, dir);
        try {
            fileOutputStream = new FileOutputStream(compressedFile);
            scaledBitmap(imageFile, maxWidth, maxHeight).compress(compressFormat, quality, fileOutputStream);
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        }
        return compressedFile;
    }


    private Bitmap scaledBitmap(@NonNull File imageFile,
                                @IntRange(from = 1, to = Integer.MAX_VALUE) int maxWidth,
                                @IntRange(from = 1, to = Integer.MAX_VALUE) int maxHeight) throws IOException {
        String imagePath = imageFile.getAbsolutePath();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, options);
        options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        ExifInterface exif = new ExifInterface(imagePath);
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
        Matrix matrix = new Matrix();
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) matrix.postRotate(90);
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) matrix.postRotate(180);
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) matrix.postRotate(270);
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return scaledBitmap;
    }


    private int calculateInSampleSize(@NonNull BitmapFactory.Options options,
                                      @IntRange(from = 1, to = Integer.MAX_VALUE) int maxWidth,
                                      @IntRange(from = 1, to = Integer.MAX_VALUE) int maxHeight) {
        int inSampleSize = 1;
        if (options.outWidth > maxWidth || options.outHeight > maxHeight) {
            final int halfWidth = options.outWidth / 2;
            final int halfHeight = options.outHeight / 2;
            while (halfWidth / inSampleSize >= maxWidth && halfHeight / inSampleSize >= maxHeight) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }


    public static class Builder {

        private int maxWidth = 900;
        private int maxHeight = 900;
        private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        private int quality = 80;

        public Builder maxWidth(@IntRange(from = 1, to = Integer.MAX_VALUE) int width) {
            maxWidth = width;
            return this;
        }

        public Builder maxHeight(@IntRange(from = 1, to = Integer.MAX_VALUE) int height) {
            maxHeight = height;
            return this;
        }

        public Builder compressFormat(@NonNull Bitmap.CompressFormat format) {
            compressFormat = format;
            return this;
        }

        public Builder quality(@IntRange(from = 1, to = 100) int quality) {
            this.quality = quality;
            return this;
        }

        public ImageCompressor build(Context context) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission.");
            } if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required READ_EXTERNAL_STORAGE permission.");
            }
            return new ImageCompressor(this);
        }
    }
}
