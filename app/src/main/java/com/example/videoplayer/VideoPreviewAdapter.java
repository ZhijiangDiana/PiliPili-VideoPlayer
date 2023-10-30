package com.example.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class VideoPreviewAdapter extends RecyclerView.Adapter<VideoPreviewAdapter.mHolder> {
    private List<previewBean> mPreviewBeans;

    public VideoPreviewAdapter(List<previewBean> list) {
        this.mPreviewBeans = list;
    }

    @NonNull
    @NotNull
    @Override
    public mHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, null);
        mHolder mHolder = new mHolder(view);
        mHolder.video_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = mHolder.getAdapterPosition();
                previewBean pBean = mPreviewBeans.get(position);
                //跳转到Player完成播放视频
                Intent openPlayer = new Intent(MainActivity.mainActivity, Player.class);
                Bundle bundle = new Bundle();

                int _id = pBean.getId();
                String videoName = pBean.getVideoName();
                String videoTag = pBean.getVideoTag();
                String videoDescription = pBean.getVideoDescription().replace("\\n", "\n");
                String uploadDate = pBean.getUploadDate();
                byte[] videoThumb = pBean.getVideoThumb();
                int playCount = pBean.getPlayCount();
                int like = pBean.getLike();
                int dispatchCount = pBean.getDispatchCount();

                bundle.putInt("id", _id);
                bundle.putString("videoName", videoName);
                bundle.putString("videoTag", videoTag);
                bundle.putString("videoDescription", videoDescription);
                bundle.putString("uploadDate", uploadDate);
                bundle.putByteArray("thumbByte", videoThumb);
                bundle.putInt("playCount", playCount);
                bundle.putInt("like", like);
                bundle.putInt("dispatchCount", dispatchCount);

                openPlayer.putExtras(bundle);
                ContextCompat.startActivity(MainActivity.mainActivity, openPlayer, bundle);
            }
        });
        return mHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull mHolder holder, int position) {
        // 绑定数据
        previewBean pBean = mPreviewBeans.get(position);

        // 设置数据
        byte[] thumbByte = pBean.getVideoThumb();
        Bitmap videoThumb = BitmapFactory.decodeByteArray(thumbByte, 0, thumbByte.length, new BitmapFactory.Options());
        holder.videoThumb.setImageBitmap(videoThumb);
        holder.videoName.setText(pBean.getVideoName());
        holder.uploadDate.setText(pBean.getUploadDate());
        holder.playCount.setText(String.valueOf(pBean.getPlayCount()));
    }

    @Override
    public int getItemCount() {
        return mPreviewBeans.size();
    }

    static class mHolder extends RecyclerView.ViewHolder {
        LinearLayoutCompat video_item;
        ImageView videoThumb;
        TextView videoName;
        TextView playCount;
        TextView uploadDate;
        public mHolder(@NonNull @NotNull View itemView) {
            super(itemView);
            // 初始化控件
            video_item = itemView.findViewById(R.id.video_item);
            videoThumb = itemView.findViewById(R.id.videoThumb);
            videoName = itemView.findViewById(R.id.videoName);
            playCount = itemView.findViewById(R.id.playCnt);
            uploadDate = itemView.findViewById(R.id.uploadDate);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private AdapterView.OnItemClickListener mOnItemClickListener;
    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }
}
