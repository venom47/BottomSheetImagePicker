package io.github.venomfortyseven.bottomsheetimagepicker.util;

import android.content.Context;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.view.Display;
import android.view.WindowManager;

public class DisplaySizeUtil {


    private static Point size;


    public static Point getSize(@NonNull Context context) {
        return size == null ? newSize(context) : size;
    }


    private static Point newSize(@NonNull Context context) {
        size = new Point();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        assert windowManager != null;
        Display display = windowManager.getDefaultDisplay();
        display.getSize(size);
        return size;
    }
}
