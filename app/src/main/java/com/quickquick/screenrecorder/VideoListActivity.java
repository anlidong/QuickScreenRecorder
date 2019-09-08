package com.quickquick.screenrecorder;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.quickquick.screenrecorder.adapter.SimpleAdapter;
import com.quickquick.screenrecorder.adapter.holder.ViewHolder;
import com.quickquick.screenrecorder.bean.VideoBean;
import com.quickquick.screenrecorder.databinding.ActivityVideoListBinding;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class VideoListActivity extends AppCompatActivity implements View.OnClickListener {

    ActivityVideoListBinding binding;
    private final String sdcard = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_list);

        binding.setOnClickListener(this);

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SimpleAdapter<VideoBean> adapter = new SimpleAdapter<>(R.layout.item_video_list, BR.video);
        binding.recyclerView.setAdapter(adapter);

        binding.tvDir.setText("存储目录" + sdcard + "/ScreenRecorder/");

        final List<VideoBean> list = new ArrayList<>();
        File dir = new File(sdcard + "/ScreenRecorder/");
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File[] files = dir.listFiles();
        if(files != null && files.length > 0) {
            for (File file : files) {
                if (file.getName().endsWith(".mp4")) {
                    VideoBean video = new VideoBean();
                    video.setVideoName(file.getName());
                    video.setVideoPath(file.getAbsolutePath());
                    list.add(video);
                }
            }
        }

        adapter.setList(list);
        if(list.size() == 0) {
            binding.tvEmpty.setVisibility(View.VISIBLE);
            binding.tvDir.setVisibility(View.GONE);
        }

        adapter.setListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ViewHolder holder, View v, int position) {
                try {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(list.get(position).getVideoPath());
                    Uri uri;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Uri contentUri = FileProvider.getUriForFile(v.getContext(), getPackageName() + ".fileprovider", file);
                        intent.setDataAndType(contentUri, "video/*");
                    } else {
                        uri = Uri.fromFile(file);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setDataAndType(uri, "video/*");
                    }

                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "视频播放错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            default:
                break;
        }
    }
}
