package com.example.myapplication1;

import java.io.Serializable;
import java.io.EOFException;
import java.io.IOException;

public class Value  implements Serializable {

    String filename,channelName;
    VideoFile video;

    Value(String filename){

        this.filename=filename;
        video=new VideoFile(filename);
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public VideoFile getVideo() {
        return video;
    }

    public void setVideo(VideoFile video) {
        this.video = video;
    }

    public void setChannelname(String channelName){
        this.channelName=channelName;
    }


}
