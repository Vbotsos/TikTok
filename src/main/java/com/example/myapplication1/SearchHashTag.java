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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class SearchHashTag extends AppCompatActivity {
    Button btnSearch;
    EditText HashParam;
    String hashTag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_search_hash_tag);

        btnSearch = (Button) findViewById(R.id.searchbtn);
        HashParam = (EditText) findViewById(R.id.hashTag);
    }

    @Override
    protected void onStart() {
        super.onStart();

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hashTag = HashParam.getText().toString();

                SearchHashTag.ClientRunner runn = new SearchHashTag.ClientRunner();

                runn.execute(hashTag);
            }
        });
    }

    private class ClientRunner extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... hashtag) {
            Log.e("error", "1");
            String fname = null;
            Socket s = null;

            try {
                int port = 8080;
                String IP = "192.168.1.5";
                s = new Socket(IP, port);


                DataOutputStream dOut = new DataOutputStream(s.getOutputStream());
                dOut.writeUTF("1");
                dOut.flush();

                Log.e("snet1","1");


                PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                out.println(hashTag);
                out.flush();

                //Now read the port of the correct Broker that you should connect
                DataInputStream dIn = new DataInputStream(s.getInputStream());
                int RightBrokerPort=dIn.readInt();

                System.out.println(RightBrokerPort+" Broker-Consumer right broker \n");

                if(port!=RightBrokerPort) {
                    s.close();
                    Socket newSocket = null;
                    newSocket=RightBroker(newSocket, RightBrokerPort);

                    //senting again hashTag
                    out = new PrintWriter(newSocket.getOutputStream(), true);
                    out.println(hashTag);
                    out.flush();

                    dIn = new DataInputStream(newSocket.getInputStream());
                    RightBrokerPort=dIn.readInt();
                    System.out.println(RightBrokerPort);

                    fname=ReceiveVideo(newSocket);

                }else{
                    fname=ReceiveVideo(s);
                    System.out.println("Video Received!!!");
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(getApplicationContext() ,Player.class);
            intent.putExtra("filename",fname);
            startActivity(intent);

            return null;

        }

        public  Socket RightBroker(Socket socket, int port) throws IOException {
            socket=new Socket("192.168.1.5", port);
            System.out.println("New Socket created: "+port);
            return socket;
        }

        String ReceiveVideo(Socket s) throws IOException, ClassNotFoundException {

            String HashTag,fname = null;

            DataInputStream dIn = new DataInputStream(s.getInputStream());
            HashTag=dIn.readUTF();
            System.out.printf("Sent from the broker the Video with HashTag: %s\n", HashTag);

            int NumberOfVideosToReceive=dIn.readInt();
            System.out.println("We Read the number of videos : "+NumberOfVideosToReceive);

            int count =0;
            while (count<NumberOfVideosToReceive) {
                int length = dIn.readInt(); // read length of incoming message
                System.out.println("We Read length of video: " + length);

                System.out.println("Now receiving the video... ");
                byte[] input = new byte[length];
                if (length > 0) {
                    int bytesRead=0;
                    while(bytesRead<length){
                        input[bytesRead]=dIn.readByte();
                        bytesRead++;
                    }
                    System.out.println("input length: " + input.length);
                    fname=chunk(HashTag,input);
                }
                count++;
            }
            dIn.close();
            return fname;
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
    }

}