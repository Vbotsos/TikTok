package com.example.myapplication1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class Player extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_player);

        Intent h=getIntent();
        String filename=h.getStringExtra("filename");


        VideoView videoView= (VideoView) findViewById(R.id.video_view);
        String videopath= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath() + "/" + filename+".mp4";
        Uri uri= Uri.parse(videopath);
        videoView.setVideoURI(uri);
        MediaController mediaController= new MediaController( this);
        videoView.setMediaController(mediaController);
        mediaController.setAnchorView(videoView);
    }
}