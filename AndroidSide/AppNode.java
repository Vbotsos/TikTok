package com.example.myapplication1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AppNode extends AppCompatActivity {


    TextView text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_node);

        text=(TextView)findViewById(R.id.textView2);
    }


    @Override
    protected void onStart() {
        super.onStart();

      //Bundle extras = getIntent().getExtras();
        byte[] data = getIntent().getByteArrayExtra("array");
//        Log.e("here", String.valueOf(data.length));


    }


}