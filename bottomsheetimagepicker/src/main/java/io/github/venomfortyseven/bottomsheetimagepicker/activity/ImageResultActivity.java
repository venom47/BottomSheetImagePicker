package io.github.venomfortyseven.bottomsheetimagepicker.activity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.venomfortyseven.bottomsheetimagepicker.listener.ImageResultListener;

/**
 * 카메라 및 갤러리 호출을 위한 액티비티.
 */
public class ImageResultActivity extends AppCompatActivity {


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REQ_CAMERA, REQ_GALLERY})
    public @interface REQ_MODE {}
    public static final int REQ_CAMERA = 1119;
    public static final int REQ_GALLERY = 1157;

    public static final String EXTRA_REQ_TYPE ="req_type";
    public static final String EXTRA_CAMERA_FILE_PATH = "camera_file_path";


    /**
     * 요청 타입. REQ_CAMERA or REQ_GALLERY.
     */
    @REQ_MODE
    private int reqType;
    /**
     * 카메라 촬영으로 생성된 파일 경로.
     */
    private @Nullable String cameraFilePath;
    /**
     * 요청 결과 처리.
     */
    private static @Nullable ImageResultListener imageResultListener;


    public static void startActivity(Context context, Intent intent, ImageResultListener listener) {
        imageResultListener = listener;
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);

        restoreInstanceState(savedInstanceState == null ? getIntent().getExtras() : savedInstanceState);

        try {
            if (reqType == REQ_CAMERA)
                requestCamera();
            else if (reqType == REQ_GALLERY)
                requestGallery();
            else finish();
        } catch (IOException e) {
            finish();
        }
    }


    private void restoreInstanceState(@Nullable Bundle bundle) {
        if (bundle != null) {
            reqType = bundle.getInt(EXTRA_REQ_TYPE);
            cameraFilePath = bundle.getString(EXTRA_CAMERA_FILE_PATH);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(EXTRA_REQ_TYPE, reqType);
        outState.putString(EXTRA_CAMERA_FILE_PATH, cameraFilePath);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
        imageResultListener = null;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case REQ_CAMERA: // 카메라.
                if (resultCode == RESULT_OK) scan();
                else finish();
                break;
            case REQ_GALLERY: // 갤러리.
                if (data != null) {
                    Uri uri = data.getData();
                    if (uri != null && imageResultListener != null)
                        imageResultListener.onReceiveGalleryResult(data.getData());
                }
                finish();
                break;
            default: finish();
        }
    }


    /**
     * 갤러리 호출.
     */
    private void requestGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_GALLERY);
    }


    /**
     * 카메라 호출.
     * @throws IOException IOException.
     */
    private void requestCamera() throws IOException {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            finish();
        }
        File cameraFile = createCameraFile(); // 사진 촬영 후 저장될 파일 생성.
        cameraFilePath = cameraFile.getAbsolutePath();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraFileUri(cameraFile));
        startActivityForResult(intent, REQ_CAMERA);
    }


    /**
     * 사진 촬영 후 저장될 파일 생성.
     * @return File.
     * @throws IOException IOException.
     */
    private File createCameraFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return  File.createTempFile(timeStamp, ".jpg", dir);
    }


    private Uri cameraFileUri(File cameraFile) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
            return Uri.fromFile(cameraFile);
        else
            return FileProvider.getUriForFile(this, getPackageName() + ".provider", cameraFile);
    }


    /**
     * 촬영 후 갤러리에 반영되도록 스캔.
     */
    private void scan() {
        if (!TextUtils.isEmpty(cameraFilePath))
            MediaScannerConnection.scanFile(this, new String[]{cameraFilePath}, new String[]{"image/jpeg"}, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(final String path, final Uri uri) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (imageResultListener != null)
                                imageResultListener.onReceiveCameraResult(Uri.fromFile(new File(cameraFilePath)));
                            finish();
                        }
                    });
                }
            });
    }

}
