package com.example.myapplication1;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;


public class VideoFile implements Serializable {
    String filename, HashTag;
    private byte[] byteArr;
     byte[] temp;
    VideoFile(String filename) {
        this.filename = filename;

    }

    void openfile(String filename, String pathExt) throws FileNotFoundException {

        String path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath();
        String Path=pathExt+"/"+"Noice.mp4";

        File file = new File(Path);
        Log.i("2",file.getAbsolutePath());
        try {
            FileInputStream inputStream = new FileInputStream(file.getAbsolutePath());
            MediaMetadataRetriever retriever2 = new MediaMetadataRetriever();
            retriever2.setDataSource(inputStream.getFD());
        }catch(FileNotFoundException e){
            Log.d("DEBUG", "FileNotFoundException", e);
        }catch(IOException ea){
            Log.d("DEBUG", "IOException", ea);
        }

        if (file.exists()) {
            Log.i(filename, ".mp4 file Exist");

            //Added in API level 10
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();

            try {
                retriever.setDataSource(file.getAbsolutePath());
                String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                long dur = Long.parseLong(duration);
                Log.i("duration",duration);
                byte[] b=retriever.getEmbeddedPicture();
                String le = String.valueOf(b.length);
                Log.i("lenght",le);
                for (int i = 0; i < 1000; i++) {
                    //only Metadata != null is printed!
                    if (retriever.extractMetadata(i) != null) {
                        Log.i(filename, "Metadata :: " + retriever.extractMetadata(i));
                    }
                }
            } catch (Exception e) {
                Log.e(filename, "Exception : " + e.getMessage());
            }
        } else {
            Log.e(filename, ".mp4 file doesn´t exist.");
        }
    }
    public byte[] convertToChunks(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int l;
        byte[] data = new byte[1024];
        while ((l = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, l);
        }
        buffer.flush();
        return buffer.toByteArray();
    }


    public void ToString() {

    }

    public String getHashTag() {
        return  "metadata.get(HashTag)";
    }

    void AddHashTag(String HashTag) {
       // metadata.add("HashTag", HashTag);
        this.HashTag=HashTag;

    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    byte[] getByteArr(){
        return temp;
    }

    public void setByte(byte [] data){
        for (int i=0;i<data.length;i++){
            temp[i]=data[i];
        }

    }
}
