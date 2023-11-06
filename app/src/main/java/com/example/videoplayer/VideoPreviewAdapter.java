package com.example.videoplayer;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.videoplayer.MainVideoList.*;

public class VideoPreviewAdapter extends RecyclerView.Adapter<VideoPreviewAdapter.mHolder> {
    public List<PreviewBean> mPreviewBeans;

    public VideoPreviewAdapter(List<PreviewBean> list) {
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
                PreviewBean pBean = mPreviewBeans.get(position);
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
                int memberOnly = pBean.getMemberOnly();

                if (Variable.currentUser.getMembership() < memberOnly) {
                    needMembership();
                    return;
                }

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
        PreviewBean pBean = mPreviewBeans.get(position);

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
    private void needMembership() {

        AlertDialog.Builder memOnlyBuilder = new AlertDialog.Builder(MainActivity.mainActivity);
        memOnlyBuilder.setTitle("此视频仅限会员观看")
                .setMessage("是否充值会员\n" +
                        "    会员连续包月每月仅需19.19元\n" +
                        "    连续包年每月仅需11.45元！\n")
                .setCancelable(false)
                .setPositiveButton("确认", yes)
                .setNegativeButton("取消", no)
                .show();
    }

    private final DialogInterface.OnClickListener yes = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {

        }
    };

    private final DialogInterface.OnClickListener no = new DialogInterface.OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    };
}
