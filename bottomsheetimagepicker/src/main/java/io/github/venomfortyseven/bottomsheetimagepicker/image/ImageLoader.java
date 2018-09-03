package io.github.venomfortyseven.bottomsheetimagepicker.image;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import java.util.ArrayList;
import java.util.List;

public class ImageLoader extends AsyncTaskLoader<List<Image>> {


    public ImageLoader(@NonNull Context context) {
        super(context);
    }


    @Nullable
    @Override
    public List<Image> loadInBackground() {
        return new ImageCursor(getContext()).fetchImages();
    }


    public static class ImageCursor {


        private @NonNull Context context;
        private @NonNull ContentResolver resolver;
        private final String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
        private final String orderBy = MediaStore.Images.Media._ID + " DESC";


        public ImageCursor(@NonNull Context context) {
            this.context = context.getApplicationContext();
            resolver = context.getContentResolver();
        }


        /**
         * @return 모든 이미지.
         */
        public List<Image> fetchImages() {
            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, orderBy);
            return fetchImages(cursor);
        }


        /**
         * @param limit Limit.
         * @return Images.
         */
        public List<Image> fetchImages(int limit) {
            String sortOrder = orderBy + " LIMIT " + limit;
            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, sortOrder);
            return fetchImages(cursor);
        }


        /**
         * @param prevId ID.
         * @param limit Limit.
         * @return Images.
         */
        public List<Image> nextImages(String prevId, int limit) {
            String selection = MediaStore.Images.Media._ID + "<?";
            String sortOrder = orderBy + " LIMIT " + limit;
            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, new String[]{prevId}, sortOrder);
            return fetchImages(cursor);
        }


        /**
         * @param cursor Cursor.
         * @return Images.
         */
        private List<Image> fetchImages(Cursor cursor) {
            List<Image> images = new ArrayList<>();
            if (cursor != null) {
                int dataColumnIndex = cursor.getColumnIndex(projection[0]);
                int idColumnIndex = cursor.getColumnIndex(projection[1]);
                int orientationColumnIndex = cursor.getColumnIndex(projection[2]);
                if (cursor.moveToFirst()) {
                    do {
                        String path = cursor.getString(dataColumnIndex);
                        String id = cursor.getString(idColumnIndex);
                        int orientation = cursor.getInt(orientationColumnIndex);
                        Image image = new Image(id, Uri.parse(path), fetchThumbnail(id), orientation);
                        if (image.getOriginal() != null && image.getThumbnail() != null)
                            images.add(image);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            }
            return images;
        }


        /**
         * @param uri Uri
         * @return Image.
         */
        public Image fetchImage(@NonNull Uri uri) {
            Image image = null;
            String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION};
            String selection = MediaStore.Images.Media.DATA + "=?";
            String[] selectionArgs = new String[]{Image.getRealPath(context, uri)};
            Cursor cursor = resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    int idColumnIndex = cursor.getColumnIndex(projection[0]);
                    int orientationColumnIndex = cursor.getColumnIndex(projection[1]);
                    if (cursor.moveToFirst()) {
                        String id = cursor.getString(idColumnIndex);
                        int orientation = cursor.getInt(orientationColumnIndex);
                        Uri thumbnailUri = fetchThumbnail(id);
                        if (thumbnailUri != null)
                            image = new Image(id, uri, fetchThumbnail(id), orientation);
                    }
                } finally {
                    cursor.close();
                }
            }
            return image;
        }


        /**
         * @param imageId ID.
         * @return Thumbnail.
         */
        public Uri fetchThumbnail(@NonNull String imageId) {
            String[] projection = {MediaStore.Images.Thumbnails.DATA};
            String selection = MediaStore.Images.Thumbnails.IMAGE_ID + "=?";
            String[] selectionArgs = new String[]{imageId};
            Cursor cursor = resolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int columnIndex = cursor.getColumnIndex(projection[0]);
                        String path = cursor.getString(columnIndex);
                        return Uri.parse(path);
                    } else {
                        long id = Long.parseLong(imageId);
                        Bitmap thumbnail = MediaStore.Images.Thumbnails.getThumbnail(resolver, id, MediaStore.Images.Thumbnails.MINI_KIND, null);
                        if (thumbnail != null) {
                            return fetchThumbnail(imageId);
                        } else return null;
                    }
                } finally {
                    cursor.close();
                }
            } else return null;
        }
    }

}
