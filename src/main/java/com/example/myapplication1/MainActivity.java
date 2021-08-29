package com.example.myapplication1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button searchash,upvideo,searchannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        Context context=this;


        searchash = (Button) findViewById(R.id.searchash);
        upvideo=(Button) findViewById(R.id.upvideo);
        searchannel=(Button) findViewById(R.id.searchannel);

                String uriPath2 = "content://com.android.providers.downloads.documents/document/downloads/24";
        Uri uri = Uri.parse(uriPath2);
        Log.i("1",uri.getPath());

        Log.i("12",Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath());
       String pathExt=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();


//        VideoFile videoFile=new VideoFile("noice.mp4");
//        try {
//            videoFile.openfile("Noice",pathExt);
//            FileInputStream is = new FileInputStream(pathExt+"/"+"Noice.mp4");
//            Log.i("Size:",String.valueOf(videoFile.convertToChunks(is).length));
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        searchash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , SearchHashTag.class);
                startActivity(intent);
            }
        });
        upvideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , UploadVideo.class);
                startActivity(intent);
            }
        });
        searchannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext() , SearchChannelName.class);
                startActivity(intent);
            }
        });
    }
}