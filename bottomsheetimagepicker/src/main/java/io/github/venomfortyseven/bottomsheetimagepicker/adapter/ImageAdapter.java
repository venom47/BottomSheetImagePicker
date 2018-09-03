package io.github.venomfortyseven.bottomsheetimagepicker.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import io.github.venomfortyseven.bottomsheetimagepicker.R;
import io.github.venomfortyseven.bottomsheetimagepicker.activity.ImageResultActivity;
import io.github.venomfortyseven.bottomsheetimagepicker.image.Image;
import io.github.venomfortyseven.bottomsheetimagepicker.image.ImageLoader;
import io.github.venomfortyseven.bottomsheetimagepicker.listener.ImageResultListener;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {


    private static final int POSITION_CAMERA = 0;
    private static final int POSITION_GALLERY = 1;

    private static final int LIMIT = 74;
    private static final int NEED_MORE_COUNT = 47;


    private @NonNull Context context;

    /**
     * 표시할 이미지들.
     */
    private List<Image> images = new ArrayList<>();
    /**
     * 선택된 이미지들.
     */
    private List<Image> checkedImages = new ArrayList<>();


    /**
     * 다중 선택 여부.
     */
    private boolean isMultiSelectable;

    /**
     * 불러올 이미지가 더 있는지 확인용.
     */
    private boolean hasMore;

    /**
     * 카메라, 갤러리 요청 결과 처리.
     */
    private @NonNull ImageResultListener imageResultListener;


    /**
     * @param context Context.
     * @param imageResultListener 카메라, 갤러리 요청 결과 처리.
     */
    public ImageAdapter(@NonNull Context context, @NonNull ImageResultListener imageResultListener) {
        this.context = context;
        this.imageResultListener = imageResultListener;
        images.add(0, null); // 카메라.
        images.add(1, null); // 갤러리.
        new ImageLoadTask(this).execute(); // 이미지 로드.
    }


    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View v = inflater.inflate(R.layout.item_image_picker, parent, false);
        return new ImageViewHolder(v);
    }


    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image image = images.get(position);
        if (position == POSITION_CAMERA) { // 카메라.
            holder.imageView.setImageResource(R.drawable.photo_camera_white_48dp);
            holder.imageView.setRotation(0);
            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(View.INVISIBLE);
        } else if (position == POSITION_GALLERY) { // 갤러리.
            holder.imageView.setImageResource(R.drawable.photo_white_48dp);
            holder.imageView.setRotation(0);
            holder.checkBox.setChecked(false);
            holder.checkBox.setVisibility(View.INVISIBLE);
        } else { // 이미지.
            holder.imageView.setImageURI(image.getThumbnail());
            holder.imageView.setRotation(image.getOrientation());
            holder.checkBox.setChecked(checkedImages.contains(image));
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        // 불러올 이미지가 있는지 확인 후 불러오기.
        int size = images.size();
        if (size - position < NEED_MORE_COUNT && hasMore) {
            hasMore = false;
            String lastImageId = images.get(size -1).getId(); // 마지막 이미지 아이디.
            new ImageLoadTask(this).execute(lastImageId);
        }
    }


    @Override
    public int getItemCount() {
        return images.size();
    }


    /**
     * 이미지 추가 후 갱신.
     * @param images 추가할 이미지.
     */
    public void addImages(@NonNull List<Image> images) {
        int positionStart = this.images.size() + 1;
        this.images.addAll(images);
        notifyItemRangeInserted(positionStart, this.images.size());
        hasMore = images.size() > 0;
    }


    /**
     * 이미지 추가 후 갱신.
     * @param image 추가할 이미지.
     * @param position 추가할 이미지 위치. 0-카메라, 1-갤러리 사용 중이므로 보통은 2.
     */
    public void addImage(@NonNull Image image, int position) {
        images.add(position, image);
        notifyItemInserted(position);
    }


    /**
     * @return 선택된 이미지.
     */
    public List<Image> getCheckedImages() {
        return checkedImages;
    }


    /**
     * @param isMultiSelectable 다중 선택 여부.
     */
    public void enableMultiSelect(boolean isMultiSelectable) {
        this.isMultiSelectable = isMultiSelectable;
    }


    class ImageViewHolder extends RecyclerView.ViewHolder {


        AppCompatImageView imageView;
        AppCompatCheckBox checkBox;


        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            checkBox = itemView.findViewById(R.id.check_box);
            checkBox.setClickable(false);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position == POSITION_CAMERA) { // 카메라.
                        requestImageResult(ImageResultActivity.REQ_CAMERA);
                    } else if (position == POSITION_GALLERY) { // 갤러리.
                        requestImageResult(ImageResultActivity.REQ_GALLERY);
                    } else { // 이미지 선택 또는 해제.
                        checkImage(position);
                    }
                }
            });
        }


        /**
         * @param reqType 카메라 또는 갤러리 요청.
         */
        private void requestImageResult(@ImageResultActivity.REQ_MODE int reqType) {
            Intent intent = new Intent(itemView.getContext(), ImageResultActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(ImageResultActivity.EXTRA_REQ_TYPE, reqType);
            intent.putExtras(bundle);
            ImageResultActivity.startActivity(itemView.getContext(), intent, imageResultListener);
        }


        /**
         * 이미지 선택 또는 해제.
         * @param position Position
         */
        private void checkImage(int position) {
            Image image = images.get(position);
            if (checkedImages.contains(image)) // 체크된 이미지는 해제.
                checkedImages.remove(image);
            else // 아니면 체크.
                checkedImages.add(image);
            notifyItemChanged(position);
            if (!isMultiSelectable && checkedImages.size() > 1) { // 다중선택모드가 아니면 이전 선택 해제.
                int prevChecked = images.indexOf(checkedImages.get(0));
                checkedImages.remove(0);
                notifyItemChanged(prevChecked);
            }
        }
    }


    public static class ImageLoadTask extends AsyncTask<String, Void, List<Image>> {

        private @NonNull ImageAdapter imageAdapter;

        ImageLoadTask(@NonNull ImageAdapter imageAdapter) {
            this.imageAdapter = imageAdapter;
        }

        @Override
        protected List<Image> doInBackground(String... ids) {
            ImageLoader.ImageCursor imageCursor = new ImageLoader.ImageCursor(imageAdapter.context);
            if (ids == null || ids.length == 0) return imageCursor.fetchImages(LIMIT); // 첫 이미지 호출.
            else return imageCursor.nextImages(ids[0], LIMIT); // 다음 이미지 호출.
        }


        @Override
        protected void onPostExecute(List<Image> images) {
            imageAdapter.addImages(images);// 이미지 추가.
        }
    }
}
