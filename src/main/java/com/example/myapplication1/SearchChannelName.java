package com.example.myapplication1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


public class SearchChannelName extends AppCompatActivity {
    public static int var = 0;
    public int port;
    Button btnSearch, button;
    TextView alert;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search_channel_name);

        btnSearch = (Button) findViewById(R.id.changeBroker);
        button = (Button) findViewById(R.id.button);
    }


    @Override
    protected void onStart() {
        super.onStart();


        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                SearchChannelName.ClientRunner runn = new SearchChannelName.ClientRunner();
                String hashTag = "2";
                runn.execute(hashTag);
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchChannelName.Runn runn = new SearchChannelName.Runn();
                String channel = (String)button.getText();
                alert=(TextView)findViewById(R.id.alert);
                alert.setText("Downloading video...");
                runn.execute(channel);


            }
        });
    }

    private class Runn extends AsyncTask<String,Void ,Void>{
        @Override
        protected Void doInBackground(String... channel) {
            try {
                String fname = null;
                Log.e("2",channel[0]);
                Socket s = null;
                String IP = "192.168.1.5";
                s = new Socket(IP, port);

//                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
//                out.println("3");
//                out.flush();

                DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
                dOut.writeUTF("3");
                dOut.flush();

               // DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
                dOut.writeUTF(channel[0]);
                dOut.flush();

                DataInputStream dIn = new DataInputStream(s.getInputStream());
                String hashTag=dIn.readUTF();
                System.out.println(hashTag);


                int length = dIn.readInt(); // read length of incoming message
                System.out.println("We Read length of video: " + length);


                System.out.println("Downloading Video... ");
                byte[] input = new byte[length];
                if (length > 0) {
                    int bytesRead=0;
                    while(bytesRead<length){
                        input[bytesRead]=dIn.readByte();
                        bytesRead++;
                    }
                    System.out.println("input length: " + input.length);
                    fname=chunk(hashTag,input);
                    System.out.println("The Video with HashTag: "+hashTag+" Received!");
                }
                dIn.close();

                Intent intent = new Intent(getApplicationContext() ,Player.class);
                intent.putExtra("filename",fname);
                startActivity(intent);
                
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }



    public String chunk(String filename,byte[] data) throws IOException {
        boolean Available= false;
        boolean Readable= false;
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)){
            // Both Read and write operations available
            Available= true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)){
            // Only Read operation available
            Available= true;
            Readable= true;
        } else {
            // SD card not mounted
            Available = false;
        }

        String FILENAME = filename+"_New";

        File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File myFile = new File(folder, FILENAME+".mp4");
        FileOutputStream fstream = new FileOutputStream(myFile);
        for(int i=0;i<data.length;i++) {
            fstream.write(data[i]);
        }
        fstream.close();
        return FILENAME;
    }



    private class ClientRunner extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... hashtag) {
            Socket s = null;

            try {

                if (var == 0) {
                    port = 8080;
                } else if (var == 1) {
                    port = 8084;
                } else {
                    port = 8088;
                }

                String IP = "192.168.1.5";
                s = new Socket(IP, port);
                var++;
                if (var > 2) {
                    var = 0;
                }


//                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
//                out.println("2");
//                out.flush();

                DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
                dOut.writeUTF("2");
                dOut.flush();

                DataInputStream dIn = new DataInputStream(s.getInputStream());
                int NumNames = dIn.readInt(); // read number of channels names
                int i = 0;
                ArrayList<String> names = new ArrayList<String>();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        button.setText("EMPTY");
                    }
                });

                while (i < NumNames) {
                    names.add(dIn.readUTF());
                    Log.e("1", names.get(i));
                    if (names.get(i) != null) {
                        String name=names.get(i);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                button.setText(name);
                            }
                        });
                    }
                    i++;
                }
                s.close();


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
}
}