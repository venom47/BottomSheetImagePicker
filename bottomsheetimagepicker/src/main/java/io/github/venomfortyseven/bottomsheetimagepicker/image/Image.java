package io.github.venomfortyseven.bottomsheetimagepicker.image;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import java.io.File;

public class Image {


    /**
     * 아이디.
     */
    private @NonNull String id;
    /**
     * 원본.
     */
    private @NonNull Uri original;
    /**
     * 썸네일.
     */
    private @NonNull Uri thumbnail;
    /**
     * 방향.
     */
    private int orientation;


    public Image(@NonNull String id, @NonNull Uri original, @NonNull Uri thumbnail, int orientation) {
        this.id = id;
        this.original = original;
        this.thumbnail = thumbnail;
        this.orientation = orientation;
    }


    public String getId() {
        return id;
    }


    public Uri getOriginal() {
        return original;
    }


    public Uri getThumbnail() {
        return thumbnail;
    }


    public int getOrientation() {
        return orientation;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;
        if (!(obj instanceof Image)) return false;
        return ((Image) obj).getOriginal().getPath().equals(original.getPath());
    }


    @Override
    public String toString() {
        return "id: " + id +
                "\noriginal: " + original.toString() +
                "\nthumbnail: " + thumbnail.toString() +
                "\norientation: " + orientation;
    }


    public File getOriginalFile(Context context) {
        return new File(getRealPath(context, original));
    }


    public File getThumbnailFile(Context context) {
        return new File(getRealPath(context, thumbnail));
    }


    /**
     * Uri 경로.
     * @param context Context.
     * @param uri Uri.
     * @return Real path.
     */
    public static String getRealPath(Context context, Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(index);
            cursor.close();
        }
        return result;
    }


    /**
     * 오리지널 보기.
     * @param imageView ImageView.
     */
    public void showOriginal(@NonNull ImageView imageView) {
        showImage(imageView, original, orientation);
    }


    /**
     * 썸네일 보기.
     * @param imageView ImageView.
     */
    public void showThumbnail(@NonNull ImageView imageView) {
        showImage(imageView, thumbnail, orientation);
    }


    /**
     * 이미지 보기.
     * @param imageView ImageView.
     * @param uri Uri.
     * @param orientation Orientation.
     */
    public static void showImage(@NonNull ImageView imageView, @NonNull Uri uri, int orientation) {
        if (orientation == 0) imageView.setImageURI(uri);
        else if (orientation == 180) {
            imageView.setImageURI(uri);
            imageView.setRotation(orientation);
        } else {
            Matrix matrix = new Matrix();
            matrix.setRotate(orientation);
            String realPath = getRealPath(imageView.getContext(), uri);
            Bitmap bitmap = BitmapFactory.decodeFile(realPath);
            if (bitmap == null) imageView.setImageURI(uri);
            else {
                Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                bitmap.recycle();
                imageView.setImageBitmap(rotated);
            }
        }
    }
}
