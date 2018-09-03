package io.github.venomfortyseven.bottomsheetimagepicker.listener;

import android.net.Uri;
import android.support.annotation.NonNull;

public interface ImageResultListener {

    void onReceiveCameraResult(@NonNull Uri uri);
    void onReceiveGalleryResult(@NonNull Uri uri);
}
