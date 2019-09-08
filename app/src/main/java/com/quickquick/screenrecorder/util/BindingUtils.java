package com.quickquick.screenrecorder.util;

import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

public class BindingUtils {
    @BindingAdapter("videoThumbnail")
    public static void setVideoThumbnail(final ImageView imageView, String videoPath) {
        imageView.setImageBitmap(ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND));
//        GlideApp.with(App.getApp())
//                .load(Uri.fromFile(new File(filePath)))
//                .transition(new DrawableTransitionOptions().crossFade())
//                .into(imageView);
    }
}
