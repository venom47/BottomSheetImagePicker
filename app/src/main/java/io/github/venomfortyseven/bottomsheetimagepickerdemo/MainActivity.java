package io.github.venomfortyseven.bottomsheetimagepickerdemo;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import io.github.venomfortyseven.bottomsheetimagepicker.BottomSheetImagePicker;
import io.github.venomfortyseven.bottomsheetimagepicker.listener.ImagePickerListener;
import io.github.venomfortyseven.bottomsheetimagepicker.image.Image;
import io.github.venomfortyseven.permissionhelper.PermissionHelper;
import io.github.venomfortyseven.permissionhelper.PermissionListener;

public class MainActivity extends AppCompatActivity {


    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);

        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PermissionHelper.Builder(MainActivity.this)
                        .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                        .listener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted() {
                                new BottomSheetImagePicker.Builder(MainActivity.this)
                                        .imagePickerListener(new ImagePickerListener() {
                                            @Override
                                            public void onPicked(@NonNull List<Image> images) {
                                                if (!images.isEmpty())
                                                    images.get(0).showOriginal(imageView);
                                            }
                                        })
                                        .create()
                                        .show(getSupportFragmentManager());
                            }

                            @Override
                            public void onPermissionDenied(List<String> list) {

                            }
                        })
                        .build()
                        .request();
            }
        });
    }
}
