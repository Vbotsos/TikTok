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
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class UploadVideo extends AppCompatActivity {

    Button btnSend;
    EditText inputParam,filenameInput,nameInput;
    String hashTag,filename,channelName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_upload_video);

        btnSend = (Button) findViewById(R.id.sendBtn);
        inputParam = (EditText)findViewById(R.id.hashInput);
        filenameInput=(EditText) findViewById(R.id.fileInput);
        nameInput=(EditText)findViewById(R.id.editTextTextPersonName);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                hashTag=inputParam.getText().toString();
                filename=filenameInput.getText().toString();
                channelName=nameInput.getText().toString();
                String pathExt= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();

                VideoFile videoFile=new VideoFile(filename+".mp4");
                try {
                    Log.e("DEBUG a:",hashTag);
                    Log.e("DEBUG b:",pathExt);
                    videoFile.openfile(hashTag,pathExt);
                    Log.e("DEBUG","dsd");
                    FileInputStream is = new FileInputStream(pathExt+"/"+filename+".mp4");
                    byte[] temp =videoFile.convertToChunks(is);
                    byte[] data=new byte[temp.length];

                    for(int i=0;i<temp.length;i++){
                        data[i]=temp[i];
                    }

                    Log.e("DEBUG c",String.valueOf(temp.length));
                    Log.e("data: ",String.valueOf(data.length));


                    ClientRunner runn = new ClientRunner();

                    runn.execute(data);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private class ClientRunner extends AsyncTask<byte[] , Void, Void> {

        @Override
        protected Void doInBackground(byte[]... data) {
        Log.e("error","1");
            Socket s = null;


            try {

                try {
                    int port=8082;
                    String IP="192.168.1.5";
                    s = new Socket(IP, port);


                    Log.e("DEBUG","1");
                    int RightBroker=NotifyBrokersForHashtags(hashTag,s);
                    Log.e("DEBUG","2");

                    Socket newSocket = null;
                    if(port!=RightBroker) {
                        Log.e("3","3");
                        s.close();

                        newSocket = new Socket(IP, RightBroker);
                        NotifyBrokersForHashtags(hashTag,newSocket);

                        System.out.println("Connected to the right Broker");
                        push(hashTag,newSocket,1,channelName,data[0]);
                       newSocket.close();
                    }else {
                        System.out.println(2);
                        push(hashTag,s,1,channelName,data[0]);
                        s.close();
                    }

                } catch (UnknownHostException unknownHost) {
                    System.err.println("You are trying to connect to an unknown host!");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    try {
                        s.close();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
        public int NotifyBrokersForHashtags(String hashTag, Socket s) throws IOException {
            DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
            dOut.writeUTF(hashTag);
            dOut.flush();

            DataInputStream dIn = new DataInputStream(s.getInputStream());
            int RightBrokerPort=dIn.readInt();
            System.out.println(RightBrokerPort);
            return RightBrokerPort;
        }

        void push(String HashTag,Socket s,int videos,String ChannelName,byte[] data) throws IOException, ClassNotFoundException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            //sent channelName to Broker
            DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
            dOut.writeUTF(ChannelName);
            dOut.flush();


            //sent HashTag to Broker
            dOut.writeUTF(hashTag);
            dOut.flush();

            //Sent number of videos to broker
            dOut.writeInt(1); // write length of the message
            dOut.flush();

            String pathExt= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
            int count=0;
            while(count<1) {

                Value v=new Value(hashTag+".mp4");
                v.setChannelname("ChannelName");
                v.video.openfile(hashTag,pathExt);
                //  v.video.AddHashTag(HashTag);

                dOut.writeInt(data.length); // write length of the message
                Log.e("DATALE",String.valueOf(data.length));
                dOut.flush();
                Log.e("Up","Uploading video...");
                int bytesRead = 0;

                while ((bytesRead) < data.length) {
                    dOut.writeByte(data[bytesRead]);
                    if (bytesRead < data.length) {
                        bytesRead++;
                    } else {
                        break;
                    }
                }
                dOut.flush();
                count++;
            }

            dOut.flush();
            dOut.close();
            s.close();
            System.out.println("Upload Of the Video ENDED");
        }
    }
}