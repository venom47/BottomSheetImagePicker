# BottomSheetImagePicker
[![](https://jitpack.io/v/venom47/BottomSheetImagePicker.svg)](https://jitpack.io/#venom47/BottomSheetImagePicker)

## Download
To get a Git project into your build:<br>
**Step 1**. Add the JitPack repository to your build file.<br>
Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2**. Add the dependency
```gradle
dependencies {
    implementation 'com.github.venom47:BottomSheetImagePicker:1.0.0'
}
```

## Usage
```java
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
```