package io.github.venomfortyseven.bottomsheetimagepicker.listener;

import android.support.annotation.NonNull;

import java.util.List;

import io.github.venomfortyseven.bottomsheetimagepicker.image.Image;

public interface ImagePickerListener {

    void onPicked(@NonNull List<Image> images);
}
