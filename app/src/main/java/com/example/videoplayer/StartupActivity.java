package com.example.videoplayer;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class StartupActivity extends AppCompatActivity {
    final int[] ad = {
            R.drawable.ad0,
            R.drawable.ad1,
    };

    final String[] adJumpURL = {
            "https://webstatic.mihoyo.com/ys/event/e20210601blue_post/vert.html?page_sn=8d04ad6701394a81&bbs_presentation_style=fullscreen&utm_source=bbs&utm_medium=ys&utm_campaign=post",
            "https://www.bilibili.com/video/BV11y4y1c78g/?spm_id_from=333.337.search-card.all.click"
    };

    int sel_AD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        Random rand = new Random();
        sel_AD = rand.nextInt(ad.length-1);

        new Timer().schedule(openAD, 1000);
        new Timer().schedule(startMainActivity, 4000);
    }

    private TimerTask openAD = new TimerTask() {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setContentView(R.layout.activity_startup_ad);
                    ((ImageView) findViewById(R.id.startupAD_ad)).setImageResource(ad[sel_AD]);
                }
            });
        }
    };

    private TimerTask startMainActivity = new TimerTask() {
        @Override
        public void run() {
            startActivity(new Intent(StartupActivity.this, MainActivity.class));
            finish();
        }
    };

    public void Jump(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse(adJumpURL[sel_AD])));
    }
}