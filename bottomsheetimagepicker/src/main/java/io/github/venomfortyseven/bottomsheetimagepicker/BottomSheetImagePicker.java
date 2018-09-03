package io.github.venomfortyseven.bottomsheetimagepicker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.venomfortyseven.bottomsheetimagepicker.adapter.ImageAdapter;
import io.github.venomfortyseven.bottomsheetimagepicker.image.Image;
import io.github.venomfortyseven.bottomsheetimagepicker.image.ImageLoader;
import io.github.venomfortyseven.bottomsheetimagepicker.listener.ImagePickerListener;
import io.github.venomfortyseven.bottomsheetimagepicker.listener.ImageResultListener;
import io.github.venomfortyseven.bottomsheetimagepicker.util.DisplaySizeUtil;

public class BottomSheetImagePicker extends BottomSheetDialogFragment implements ImageResultListener {


    public static final String TAG = BottomSheetImagePicker.class.getSimpleName();

    private static final int SPAN_COUNT_PORTRAIT = 4;
    private static final int SPAN_COUNT_LANDSCAPE = 7;


    private View contentView;
    private BottomSheetBehavior bottomSheetBehavior;
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;

    private Builder builder;


    private BottomSheetBehavior.BottomSheetCallback bottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {

        @Override
        public void onStateChanged(@NonNull View view, int newState) {
            // 화면 아래로 내려가서 가려지면 종료.
            if (newState == BottomSheetBehavior.STATE_HIDDEN) dismissAllowingStateLoss();
        }

        @Override
        public void onSlide(@NonNull View view, float slideOffset) {

        }
    };


    @SuppressWarnings("ConstantConditions")
    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        contentView = View.inflate(getContext(), R.layout.image_picker_content_view, null);
        dialog.setContentView(contentView);

        initBottomSheetBehavior();
        initContentView();
    }


    private void initBottomSheetBehavior() {
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        layoutParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        if (layoutParams.getBehavior() != null) {
            bottomSheetBehavior = (BottomSheetBehavior) layoutParams.getBehavior();
            bottomSheetBehavior.setBottomSheetCallback(bottomSheetCallback);
            bottomSheetBehavior.setPeekHeight(peekHeight());
        } else {
            Log.e(TAG, "Behavior is null.");
            dismissAllowingStateLoss();
        }
    }


    private void initContentView() {
        Context context = getContext();
        if (context != null) {
            contentView.setBackgroundColor(builder.backgroundColor);

            View header = contentView.findViewById(R.id.header);
            header.setBackgroundColor(builder.headerBackgroundColor);

            AppCompatButton doneBtn = contentView.findViewById(R.id.btn_done);
            doneBtn.setTextColor(builder.doneColor);
            doneBtn.setText(builder.doneText);
            doneBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (builder.imagePickerListener != null)
                        builder.imagePickerListener.onPicked(imageAdapter.getCheckedImages());
                    dismissAllowingStateLoss();
                }
            });

            TextView titleView = contentView.findViewById(R.id.text_title);
            titleView.setTextColor(builder.titleColor);
            titleView.setText(builder.title);

            recyclerView = contentView.findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), spanCount(getResources().getConfiguration().orientation)));
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemAnimator(null);
            imageAdapter = new ImageAdapter(context, this);
            imageAdapter.enableMultiSelect(builder.isMultiSelectable);
            recyclerView.setAdapter(imageAdapter);
        } else {
            Log.e(TAG, "Context is null.");
            dismissAllowingStateLoss();
        }
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        bottomSheetBehavior.setPeekHeight(peekHeight());
        ((GridLayoutManager) recyclerView.getLayoutManager()).setSpanCount(spanCount(newConfig.orientation));

    }


    private int peekHeight() {
        Context context = getContext();
        if (context != null) return (int) (DisplaySizeUtil.getSize(getContext()).y * 0.7f);
        else return 0;
    }


    private int spanCount(int orientation) {
        return orientation == Configuration.ORIENTATION_PORTRAIT ? SPAN_COUNT_PORTRAIT : SPAN_COUNT_LANDSCAPE;
    }


    public void setBuilder(@NonNull Builder builder) {
        this.builder = builder;
    }


    public void show(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null)
            fragmentManager.beginTransaction().add(this, TAG).commit();
    }


    public void showAllowingStateLoss(FragmentManager fragmentManager) {
        if (fragmentManager.findFragmentByTag(TAG) == null)
            fragmentManager.beginTransaction().add(this, TAG).commitAllowingStateLoss();
    }

    @Override
    public void onReceiveCameraResult(@NonNull Uri uri) {
        Context context = getContext();
        if (context != null) {
            Image image = new ImageLoader.ImageCursor(getContext()).fetchImage(uri);
            if (image != null)
                imageAdapter.addImage(image, 2);
        }
    }

    @Override
    public void onReceiveGalleryResult(@NonNull Uri uri) {
        Context context = getContext();
        if (context != null) {
            Image image = new ImageLoader.ImageCursor(getContext()).fetchImage(uri);
            if (image == null) Log.e("Picker", "image is null!");
            if (image != null && builder.imagePickerListener != null) {
                Log.e("Picker", "" + image);
                List<Image> pickedImages = new ArrayList<>();
                pickedImages.add(image);
                builder.imagePickerListener.onPicked(pickedImages);
                dismissAllowingStateLoss();
            }
        }
    }


    public static class Builder {


        private Context context;

        @ColorInt
        private int doneColor = Color.BLACK;
        @ColorInt
        private int titleColor = Color.BLACK;
        @ColorInt
        private int backgroundColor = Color.LTGRAY;
        @ColorInt
        private int headerBackgroundColor = Color.WHITE;
        private String title;
        private String doneText;
        private boolean isMultiSelectable;
        private ImagePickerListener imagePickerListener;


        public Builder(@NonNull Context context) {
            this.context = context.getApplicationContext();
        }


        public Builder textColor(@ColorInt int color) {
            doneColor = color;
            titleColor = color;
            return this;
        }


        public Builder textColorRes(@ColorRes int res) {
            return textColor(ContextCompat.getColor(context, res));
        }


        public Builder doneColor(@ColorInt int color) {
            doneColor = color;
            return this;
        }


        public Builder doneColorRes(@ColorRes int res) {
            return doneColor(ContextCompat.getColor(context, res));
        }


        public Builder titleColor(@ColorInt int color) {
            titleColor = color;
            return this;
        }


        public Builder titleColorRes(@ColorRes int res) {
            return titleColor(ContextCompat.getColor(context, res));
        }


        public Builder backgroundColor(@ColorInt int color) {
            backgroundColor = color;
            return this;
        }


        public Builder backgroundColorRes(@ColorRes int res) {
            return backgroundColor(ContextCompat.getColor(context, res));
        }


        public Builder headerBackgroundColor(@ColorInt int color) {
            headerBackgroundColor = color;
            return this;
        }


        public Builder headerBackgroundColorRes(@ColorRes int res) {
            return headerBackgroundColor(ContextCompat.getColor(context, res));
        }


        public Builder title(String text) {
            title = text;
            return this;
        }


        public Builder title(@StringRes int res) {
            return title(context.getString(res));
        }


        public Builder doneText(String text) {
            doneText = text;
            return this;
        }


        public Builder doneText(@StringRes int res) {
            return doneText(context.getString(res));
        }


        public Builder enableMultiSelect(boolean isMultiSelectable) {
            this.isMultiSelectable = isMultiSelectable;
            return this;
        }


        public Builder imagePickerListener(ImagePickerListener listener) {
            imagePickerListener = listener;
            return this;
        }


        public BottomSheetImagePicker create() {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required WRITE_EXTERNAL_STORAGE permission.");
            } if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required READ_EXTERNAL_STORAGE permission.");
            } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                throw new RuntimeException("Missing required CAMERA permission.");
            }
            if (TextUtils.isEmpty(title)) title = context.getString(R.string.title);
            if (TextUtils.isEmpty(doneText)) doneText = context.getString(R.string.done);
            BottomSheetImagePicker picker = new BottomSheetImagePicker();
            picker.setBuilder(this);
            return picker;
        }
    }

}
